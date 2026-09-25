package com.benkih.estore.payout.dto.response;

import com.benkih.estore.common.enums.CurrencyCode;
import com.benkih.estore.common.enums.PaymentProvider;
import com.benkih.estore.payout.enums.PayoutStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class PayoutResponseDto {
  private String slug;
  private String payoutNumber;
  private String businessSlug;
  private String bankAccountSlug;
  private String accountName;
  private String accountNumber;
  private String bankCode;
  private String bankName;
  private BigDecimal grossAmount;
  private BigDecimal transferFee;
  private BigDecimal stampDuty;
  private BigDecimal netAmount;
  private CurrencyCode currency;
  private PayoutStatus status;
  private PaymentProvider provider;
  private String providerReference;
  private String failureReason;
  private Instant requestedAt;
  private Instant processedAt;
}
