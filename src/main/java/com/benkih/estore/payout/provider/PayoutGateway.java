package com.benkih.estore.payout.provider;

import com.benkih.estore.business.entity.BankAccount;
import com.benkih.estore.payout.dto.response.PayoutRecipientResult;
import com.benkih.estore.payout.dto.response.PayoutResult;
import com.benkih.estore.payout.dto.response.PayoutVerificationResult;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.payout.entity.PayoutRecipient;
import com.benkih.estore.payout.enums.PayoutProvider;
import com.benkih.estore.payout.enums.PayoutTransferStatus;

public interface PayoutGateway {

  PayoutProvider supports();

  PayoutRecipientResult createRecipient(
      BankAccount bankAccount
  );

  PayoutResult initiate(
      Payout payout,
      PayoutRecipient recipient
  );

  PayoutVerificationResult verify(
      String providerReference
  );
}
