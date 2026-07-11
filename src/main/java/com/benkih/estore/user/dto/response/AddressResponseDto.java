package com.benkih.estore.user.dto.response;

import com.benkih.estore.common.enums.Country;
import lombok.Data;

@Data
public class AddressResponseDto {
  private String slug;

  private String fullName;

//  private String firstName;

//  private String lastName;

  private String phoneNumber;

  private String addressLine1;

  private String addressLine2;

  private String city;

  private String state;

  private Country country;

  private String postalCode;

  private Boolean defaultAddress;
}
