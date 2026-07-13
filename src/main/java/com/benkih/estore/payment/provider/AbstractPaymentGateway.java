package com.benkih.estore.payment.provider;

import com.benkih.estore.common.exceptions.BadRequestException;
import com.benkih.estore.payment.dto.request.InitializePaymentRequest;

public abstract class AbstractPaymentGateway implements PaymentGateway {

  protected void validate(InitializePaymentRequest request){

    if(request.getAmount() == null){
      throw new BadRequestException("Amount is required");
    }

    if(request.getEmail() == null){
      throw new BadRequestException("Email is required");
    }
  }
}
