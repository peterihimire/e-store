package com.benkih.estore.refund.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.payment.dto.response.RefundPaymentResponse;
import com.benkih.estore.refund.dto.request.CreateRefundRequest;
import com.benkih.estore.refund.dto.response.RefundResponse;
import com.benkih.estore.refund.service.IRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/refunds")
@RequiredArgsConstructor
public class RefundController {
  private final IRefundService refundService;


  @PostMapping("/refund/add")
  public ResponseEntity<ApiResponse> requestRefund(@Valid @RequestBody CreateRefundRequest request) {

    RefundResponse refundResponse = refundService.requestRefund(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(new ApiResponse(
            "success",
            "Refund request sent successfully",
            refundResponse
        ));
  }


  @PostMapping("/{refundSlug}/initialize")
  @PreAuthorize("hasAuthority('REFUND_APPROVE')")
  public ResponseEntity<RefundPaymentResponse> initializeRefund(@PathVariable String refundSlug) {
    return ResponseEntity.ok(
        refundService.initializeRefund(refundSlug)
    );
  }

  /**
   * Verifies refund status with the payment gateway.
   */
  @GetMapping("/{refundReference}/verify")
  public ResponseEntity<RefundPaymentResponse> verifyRefund(
      @PathVariable String refundReference
  ) {
    return ResponseEntity.ok(
        refundService.verifyRefund(refundReference)
    );
  }

  /**
   * Get a refund by its internal slug.
   */
  @GetMapping("/{refundSlug}")
  public ResponseEntity<RefundResponse> getRefund(
      @PathVariable String refundSlug
  ) {
    return ResponseEntity.ok(
        refundService.getRefund(refundSlug)
    );
  }

  /**
   * Get refunds belonging to a user.
   */
  @GetMapping("/user/{userSlug}")
  public ResponseEntity<List<RefundResponse>> getUserRefunds(
      @PathVariable String userSlug
  ) {
    return ResponseEntity.ok(
        refundService.getUserRefunds(userSlug)
    );
  }

  /**
   * Get refunds belonging to an order.
   */
  @GetMapping("/order/{orderSlug}")
  public ResponseEntity<List<RefundResponse>> getOrderRefunds(
      @PathVariable String orderSlug
  ) {
    return ResponseEntity.ok(
        refundService.getOrderRefunds(orderSlug)
    );
  }

  /**
   * Admin approves a refund request.
   */
  @PostMapping("/{refundSlug}/approve")
  @PreAuthorize("hasAuthority('REFUND_APPROVE')")
  public ResponseEntity<RefundResponse> approveRefund(
      @PathVariable String refundSlug
  ) {
    return ResponseEntity.ok(
        refundService.approveRefund(refundSlug)
    );
  }

  /**
   * Admin rejects a refund request.
   */
  @PostMapping("/{refundSlug}/reject")
  @PreAuthorize("hasAuthority('REFUND_REJECT')")
  public ResponseEntity<RefundResponse> rejectRefund(
      @PathVariable String refundSlug,
      @RequestParam String reason
  ) {
    return ResponseEntity.ok(
        refundService.rejectRefund(refundSlug, reason)
    );
  }
}
