package com.benkih.estore.refund.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRefundRequest {

    @NotBlank private String orderSlug;

    // Customer/Admin reason.
    @NotBlank
    @Size(max = 1000)
    private String reason;

    @NotEmpty @Valid List<RefundItemRequest> items;
}
