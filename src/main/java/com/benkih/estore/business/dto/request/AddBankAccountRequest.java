package com.benkih.estore.business.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AddBankAccountRequest {
  @NotBlank(message = "Bank code is required")
  private String bankCode;

  @NotBlank(message = "Bank name is required")
  private String bankName;

  @NotBlank(message = "Account number is required")
  @Pattern(
      regexp = "\\d{10}",
      message = "Account number must be 10 digits"
  )
  private String accountNumber;

  @NotBlank(message = "Account name is required")
  private String accountName;
}
