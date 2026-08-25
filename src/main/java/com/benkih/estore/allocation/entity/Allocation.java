package com.benkih.estore.allocation.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.common.enums.AllocationStatus;
import com.benkih.estore.common.enums.Currency;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "allocations")
public class Allocation extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

//  Allocation ────────> OrderItem
//  many                                        one
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_item_id", nullable = false)
  private OrderItem orderItem;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  /**
   * Amount attributable to the business before deductions.
   */
  @Column(
      name = "gross_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal grossAmount = BigDecimal.ZERO;

  /**
   * Benkih's platform commission.
   */
  @Column(
      name = "platform_fee",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal platformFee = BigDecimal.ZERO;

  /**
   * Payment processor fee attributable to this allocation.
   */
  @Column(
      name = "payment_fee",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal paymentFee = BigDecimal.ZERO;

  /**
   * Amount already refunded against this allocation.
   *
   * This should normally be updated when a refund is successfully
   * processed rather than when a refund is merely requested.
   */
  @Column(
      name = "refund_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal refundAmount = BigDecimal.ZERO;

  /**
   * Amount remaining for the business after fees and refunds.
   *
   * Depending on your accounting model, this can be calculated as:
   *
   * grossAmount
   * - platformFee
   * - paymentFee
   * - refundAmount
   */
  @Column(
      name = "net_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal netAmount = BigDecimal.ZERO;

  @Column(
      name = "discount_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(
      name = "tax_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(
      name = "shipping_amount",
      nullable = false,
      precision = 18,
      scale = 2
  )
  private BigDecimal shippingAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AllocationStatus status = AllocationStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private Currency currency = Currency.NGN;
}