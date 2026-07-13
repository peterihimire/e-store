package com.benkih.estore.payment.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitializePaymentResponse {

  private String authorizationUrl;

  private String accessCode;

  private String reference;
}
