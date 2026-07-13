package com.benkih.estore.user.dto.request;

import com.benkih.estore.common.enums.Country;
import lombok.Data;

@Data
public class UpdateAddressRequest {
//  private String fullName;

//  @NotBlank
  private String firstName;

//  @NotBlank
  private String lastName;

  private String phoneNumber;

  private String addressLine1;

  private String addressLine2;

  private String city;

  private String state;

  private Country country;

  private String postalCode;

  private Boolean defaultAddress; // true, false, null[Boolean]
}
