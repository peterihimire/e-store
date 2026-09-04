package com.benkih.estore.allocation.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.allocation.repository.AllocationRepository;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationService implements IAllocationService{
  private final AllocationRepository allocationRepository;
  private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.0500"); // 5%

  @Transactional
  @Override
  public void allocatePayment(Payment payment) {
    if (payment == null) {
      throw new IllegalArgumentException("Payment is required");
    }

    // Prevent duplicate allocations when a webhook is retried
    if (allocationRepository.existsByPaymentId(payment.getId())) {
      log.info(
          "Allocations already exist for payment {}. Skipping allocation.",
          payment.getReference()
      );
      return;
    }

    Order order = payment.getOrder();

    if (order == null) {
      throw new IllegalArgumentException("Payment is not associated with an order");
    }

    List<OrderItem> orderItems = order.getOrderItems();
    if (orderItems == null || orderItems.isEmpty()) {
      log.warn(
          "Order {} has no order items. Nothing to allocate.",
          order.getOrderNumber()
      );
      return;
    }

    BigDecimal orderSubtotal = defaultZero(order.getSubTotal());
    if (orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Order subtotal must be greater than zero");
    }

    for (OrderItem orderItem : orderItems) {
      /*
       * OrderItem.subtotal is the authoritative gross merchandise
       * amount for this item:
       *
       * price × quantity
       */
      BigDecimal grossAmount = defaultZero(orderItem.getSubtotal());
      /*
       * Discount is already calculated at OrderItem level.
       * Do NOT redistribute Order.discountAmount here.
       */
      BigDecimal discountAmount = defaultZero(orderItem.getDiscountAmount());

      /*
       * Tax is already calculated at OrderItem level.
       * Do NOT redistribute Order.taxAmount here.
       */

      BigDecimal taxAmount = defaultZero(orderItem.getTaxAmount());
      /*
       * Shipping exists at Order level, so it needs to be
       * allocated across the order items.
       */

      BigDecimal shippingAmount = calculateShippingAllocation(
              order,
              grossAmount,
              orderSubtotal
          );

      /*
       * Benkih marketplace commission.
       */

      BigDecimal platformFee = calculatePlatformFee(grossAmount);

      /*
       * Paystack/payment processor fee exists at Payment level,
       * so it needs to be allocated across the order items.
       */

      BigDecimal processorFee = calculatePaymentFee(
              grossAmount,
              orderSubtotal,
              payment
          );
      /*
       * Seller's allocated amount.
       *
       * Tax is tracked separately because it is not necessarily
       * seller revenue.
       *
       * Shipping is currently included because the existing
       * allocation model treats allocated shipping as belonging
       * to the business. If Benkih later owns the logistics
       * revenue/cost separately, this should be adjusted.
       */

      BigDecimal netAmount = calculateNetAmount(
              grossAmount,
              discountAmount,
//              shippingAmount,
              platformFee,
              processorFee
          );

      Allocation allocation = new Allocation();
      allocation.setPayment(payment);
      allocation.setOrderItem(orderItem);
      allocation.setBusiness(orderItem.getBusiness());
      allocation.setGrossAmount(grossAmount);
      allocation.setDiscountAmount(discountAmount);
      allocation.setTaxAmount(taxAmount);
      allocation.setShippingAmount(shippingAmount);
      allocation.setPlatformFee(platformFee);
      allocation.setPaymentFee(processorFee);
      allocation.setRefundAmount(BigDecimal.ZERO);
      allocation.setNetAmount(netAmount);
      allocation.setCurrency(order.getCurrency());

      allocationRepository.save(allocation);

      log.info(
          "Created allocation for payment={}, order={}, orderItem={}, business={}, gross={}, discount={}, tax={}, shipping={}, platformFee={}, processorFee={}, net={}",
          payment.getReference(),
          order.getOrderNumber(),
          orderItem.getSlug(),
          orderItem.getBusiness().getSlug(),
          grossAmount,
          discountAmount,
          taxAmount,
          shippingAmount,
          platformFee,
          processorFee,
          netAmount
      );
    }
  }

  private BigDecimal calculateNetAmount(
      BigDecimal grossAmount,
      BigDecimal discountAmount,
//      BigDecimal shippingAmount,
      BigDecimal platformFee,
      BigDecimal paymentFee
  ) {
    return grossAmount
        .subtract(discountAmount)
        .subtract(platformFee)
        .subtract(paymentFee)
//        .add(shippingAmount)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculatePlatformFee(
      BigDecimal grossAmount
  ) {
    if (grossAmount == null ||
        grossAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    return grossAmount
        .multiply(PLATFORM_FEE_RATE)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculatePaymentFee(
      BigDecimal grossAmount,
      BigDecimal orderSubtotal,
      Payment payment
  ) {
    BigDecimal processorFee = defaultZero(payment.getProcessorFee());

    if (processorFee.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    if (orderSubtotal == null ||
        orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    BigDecimal ratio =
        grossAmount.divide(
            orderSubtotal,
            10,
            RoundingMode.HALF_UP
        );

    return processorFee
        .multiply(ratio)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateShippingAllocation(
      Order order,
      BigDecimal grossAmount,
      BigDecimal orderSubtotal
  ) {

    BigDecimal shippingFee = defaultZero(order.getShippingFee());

    if (shippingFee.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    if (orderSubtotal == null ||
        orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2);
    }

    BigDecimal ratio = grossAmount.divide(
            orderSubtotal,
            10,
            RoundingMode.HALF_UP
        );

    return shippingFee
        .multiply(ratio)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal defaultZero(BigDecimal value) {
    return value != null
        ? value
        : BigDecimal.ZERO.setScale(2);
  }
}
