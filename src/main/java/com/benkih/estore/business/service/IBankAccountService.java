package com.benkih.estore.business.service;

import com.benkih.estore.business.dto.request.AddBankAccountRequest;
import com.benkih.estore.business.dto.response.BankAccountResponseDto;

public interface IBankAccountService {
  BankAccountResponseDto addBankAccount(AddBankAccountRequest request, Long businessId);

//  BankAccount addBankAccount(
//      AddBankAccountRequest request,
//      String businessSlug
//  );
}
