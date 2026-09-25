package com.benkih.estore.payout.controller;


import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.payout.dto.request.PayoutRequestDto;
import com.benkih.estore.payout.dto.response.PayoutResponseDto;
import com.benkih.estore.payout.service.IPayoutService;
import com.benkih.estore.payout.service.PayoutService;
import com.benkih.estore.security.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/payouts")
@RequiredArgsConstructor
public class PayoutController {
  private final TenantContext tenantContext;
  private final IPayoutService payoutService;

  @PostMapping("/request")
  public ResponseEntity<ApiResponse> requestPayout(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody PayoutRequestDto request
  ) {
    Long businessId = tenantContext.getBusinessId();

    PayoutResponseDto response = payoutService.requestPayout(
        businessId,
        request.getBankAccountSlug(),
        request.getCurrency(),
        request.getAmount(),
        idempotencyKey
    );

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(new ApiResponse(
            "success",
            "Payout request successful",
            response));
  }
}
