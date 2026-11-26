package com.benkih.estore.business.controller;

import com.benkih.estore.business.dto.request.AddBankAccountRequest;
import com.benkih.estore.business.dto.response.BankAccountResponseDto;
import com.benkih.estore.business.service.BankAccountService;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.security.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Bank Accounts", description = "Seller bank accounts for payouts")
@RestController
@RequestMapping("${api.prefix}/bankAccounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final TenantContext tenantContext;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<BankAccountResponseDto>> addBankAccount(
            @Valid @RequestBody AddBankAccountRequest request) {
        Long businessId = tenantContext.getBusinessId();

        BankAccountResponseDto response = bankAccountService.addBankAccount(request, businessId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Bank account added successfully", response));
    }
}
