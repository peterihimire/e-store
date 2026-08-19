package com.benkih.estore.refund.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRefundRequest {

  @NotBlank
  private String orderSlug;

 // Customer/Admin reason.
  @NotBlank
  @Size(max = 1000)
  private String reason;

  @NotBlank
  List<RefundItemRequest> items;
}
