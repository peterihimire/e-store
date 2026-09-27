package com.benkih.estore.payout.gateway;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.payout.dto.response.PayoutRecipientResult;
import com.benkih.estore.payout.dto.response.PayoutResult;
import com.benkih.estore.payout.dto.response.PayoutVerificationResult;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.payout.entity.PayoutRecipient;
import com.benkih.estore.payout.enums.PayoutProvider;
import com.benkih.estore.payout.enums.PayoutTransferStatus;
import com.benkih.estore.payout.provider.PayoutGateway;
import com.benkih.estore.vendor.PaystackClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaystackPayoutGateway implements PayoutGateway {
  private final PaystackClient paystackClient;

  @Override
  public PayoutProvider supports() {
    return PayoutProvider.PAYSTACK;
  }


  @Override
  public PayoutRecipientResult createRecipient(
      BankAccount bankAccount
  ) {
    return paystackClient.createTransferRecipient(bankAccount);
  }


  @Override
  public PayoutResult initiate(
      Payout payout,
      PayoutRecipient recipient
  ) {
    return paystackClient.initiateTransfer(
        payout,
        recipient
    );
  }


  @Override
  public PayoutVerificationResult  verify(
      String providerReference
  ) {
    return paystackClient.verifyTransfer(providerReference);
  }
}
