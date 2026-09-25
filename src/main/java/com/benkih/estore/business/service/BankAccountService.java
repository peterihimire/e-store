package com.benkih.estore.business.service;

import com.benkih.estore.business.dto.request.AddBankAccountRequest;
import com.benkih.estore.business.dto.response.BankAccountResponseDto;
import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.business.entity.Business;
import com.benkih.estore.business.repository.BankAccountRepository;
import com.benkih.estore.business.repository.BusinessRepository;
import com.benkih.estore.common.exceptions.AlreadyExistsException;
import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountService implements IBankAccountService {
  private final BankAccountRepository bankAccountRepository;
  private final BusinessRepository businessRepository;

  @Override
  public BankAccountResponseDto addBankAccount(
      AddBankAccountRequest request,
      Long businessId
  ) {

    Business business = businessRepository
        .findById(businessId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Business not found")
        );

    if (bankAccountRepository.existsByBusinessIdAndAccountNumber(
        business.getId(),
        request.getAccountNumber()
    )) {
      throw new AlreadyExistsException("Bank account already exists");
    }

    BankAccount bankAccount = new BankAccount();

    bankAccount.setBusiness(business);
    bankAccount.setBankCode(request.getBankCode());
    bankAccount.setBankName(request.getBankName());
    bankAccount.setAccountNumber(request.getAccountNumber());
    bankAccount.setAccountName(request.getAccountName());
    bankAccount.setVerified(false);
    bankAccount.setDefaultAccount(false);

    bankAccount = bankAccountRepository.save(bankAccount);

    return convertToDto(bankAccount);
  }

  private BankAccountResponseDto convertToDto(
      BankAccount bankAccount
  ) {

    return BankAccountResponseDto.builder()
        .slug(bankAccount.getSlug())
        .bankCode(bankAccount.getBankCode())
        .bankName(bankAccount.getBankName())
        .accountName(bankAccount.getAccountName())
        .accountNumber(maskAccountNumber(
            bankAccount.getAccountNumber()
        ))
        .verified(bankAccount.isVerified())
        .defaultAccount(bankAccount.isDefaultAccount())
        .build();
  }

  private String maskAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.length() <= 4) {
      return accountNumber;
    }

    return "*".repeat(accountNumber.length() - 4)
        + accountNumber.substring(accountNumber.length() - 4);
  }
}
