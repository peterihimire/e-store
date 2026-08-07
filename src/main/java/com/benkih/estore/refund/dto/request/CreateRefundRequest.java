package com.benkih.estore.refund.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRefundRequest {

  /**
   * Order to refund.
   */
  @NotBlank
  private String orderSlug;

  /**
   * Optional.
   * If null, refund the full payment amount.
   */
  @Positive
  private BigDecimal amount;

  /**
   * Customer/Admin reason.
   */
  @NotBlank
  @Size(max = 1000)
  private String reason;
}
