package com.benkih.estore.business.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BankAccountResponseDto {

  private String slug;
  private String bankCode;
  private String bankName;
  private String accountName;
  private String accountNumber;
  private boolean verified;
  private boolean defaultAccount;
}