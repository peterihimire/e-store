package com.benkih.estore.payment.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitializePaymentResponse {
  private boolean success;
  private String authorizationUrl;
  private String accessCode;
  private String reference;
  private String message;
//  private String errorCode;
}
