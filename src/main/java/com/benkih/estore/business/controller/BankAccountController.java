package com.benkih.estore.business.controller;

import com.benkih.estore.business.dto.request.AddBankAccountRequest;
import com.benkih.estore.business.dto.response.BankAccountResponseDto;
import com.benkih.estore.business.service.IBankAccountService;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/bankAccounts")
@RequiredArgsConstructor
public class BankAccountController {
  private final IBankAccountService bankAccountService;
  private final TenantContext tenantContext;

  @PostMapping("/add")
  public ResponseEntity<ApiResponse> addBankAccount(
      @Valid @RequestBody AddBankAccountRequest request
  ) {
    Long businessId = tenantContext.getBusinessId();

    BankAccountResponseDto response = bankAccountService
        .addBankAccount(request, businessId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(new ApiResponse(
            "success",
            "Bank account added successfully",
            response));
  }
}
