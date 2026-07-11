package com.benkih.estore.user.dto.request;

import com.benkih.estore.common.enums.Country;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAddressRequest {

  @NotBlank
  private String fullName;

//  @NotBlank
//  private String firstName;

//  @NotBlank
//  private String lastName;

  @NotBlank
  private String phoneNumber;

  @NotBlank
  private String addressLine1;

  private String addressLine2;

  @NotBlank
  private String city;

  @NotBlank
  private String state;

  @NotBlank
  private Country country;

  @NotBlank
  private String postalCode;

  private boolean defaultAddress;
}
