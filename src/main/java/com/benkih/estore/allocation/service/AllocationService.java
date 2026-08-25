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

    Order order = payment.getOrder();

    List<OrderItem> orderItems = order.getOrderItems();

    BigDecimal subtotal = order.getSubTotal();

    for (OrderItem orderItem : orderItems) {

      BigDecimal grossAmount = orderItem.getPrice()
              .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

      BigDecimal itemRatio = grossAmount.divide(
              subtotal,
              10,
              RoundingMode.HALF_UP
          );

      BigDecimal discountAmount = order.getDiscountAmount()
              .multiply(itemRatio)
              .setScale(2, RoundingMode.HALF_UP);

      BigDecimal taxAmount = order.getTaxAmount()
              .multiply(itemRatio)
              .setScale(2, RoundingMode.HALF_UP);

      BigDecimal shippingAmount = calculateShippingAllocation(
              order,
              orderItem
          );

      BigDecimal platformFee = calculatePlatformFee(grossAmount);

      BigDecimal paymentFee = calculatePaymentFee(
              grossAmount,
              subtotal,
              payment
          );

      BigDecimal netAmount = calculateNetAmount(
              grossAmount,
              discountAmount,
              shippingAmount,
              platformFee,
              paymentFee
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
      allocation.setPaymentFee(paymentFee);

      allocation.setRefundAmount(BigDecimal.ZERO);
      allocation.setNetAmount(netAmount);

      allocation.setCurrency(order.getCurrency());

      allocationRepository.save(allocation);
    }
  }

  private BigDecimal calculateNetAmount(
      BigDecimal grossAmount,
      BigDecimal discountAmount,
      BigDecimal shippingAmount,
      BigDecimal platformFee,
      BigDecimal paymentFee
  ) {
    return grossAmount
        .subtract(discountAmount)
        .subtract(platformFee)
        .subtract(paymentFee)
        .add(shippingAmount)
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
    if (payment.getProcessorFee() == null ||
        payment.getProcessorFee().compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    if (orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal ratio =
        grossAmount.divide(
            orderSubtotal,
            10,
            RoundingMode.HALF_UP
        );

    return payment.getProcessorFee()
        .multiply(ratio)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateShippingAllocation(
      Order order,
      OrderItem orderItem
  ) {
    if (order.getShippingFee() == null ||
        order.getShippingFee().compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal itemGross =
        orderItem.getPrice()
            .multiply(
                BigDecimal.valueOf(orderItem.getQuantity())
            );

    BigDecimal ratio =
        itemGross.divide(
            order.getSubTotal(),
            10,
            RoundingMode.HALF_UP
        );

    return order.getShippingFee()
        .multiply(ratio)
        .setScale(2, RoundingMode.HALF_UP);
  }
}
