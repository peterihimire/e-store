package com.benkih.estore.payout.dto.request;

import com.benkih.estore.common.enums.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayoutRequestDto {

  @NotBlank(message = "Bank account is required")
  private String bankAccountSlug;

  @NotNull(message = "Amount is required")
  @DecimalMin(
      value = "0.01",
      message = "Payout amount must be greater than zero"
  )
  private BigDecimal amount;

  @NotNull(message = "Currency is required")
  private CurrencyCode currency;
}
