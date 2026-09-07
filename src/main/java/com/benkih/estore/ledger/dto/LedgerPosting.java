package com.benkih.estore.ledger.dto;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.ledger.entity.LedgerAccount;
import com.benkih.estore.ledger.enums.LedgerEntryDirection;
import com.benkih.estore.ledger.enums.LedgerEntryType;
import com.benkih.estore.payout.entity.Payout;
import com.benkih.estore.settlement.entity.Settlement;

import java.math.BigDecimal;


public record LedgerPosting(
    LedgerAccount account,
//    LedgerEntryType entryType,
    LedgerEntryDirection direction,
    BigDecimal amount,
    Allocation allocation,
    Settlement settlement,
    Payout payout,
    String description
) {
  public LedgerPosting(
      LedgerAccount account,
      LedgerEntryType entryType,
      LedgerEntryDirection direction,
      BigDecimal amount
  ) {
    this(
        account,
//        entryType,
        direction,
        amount,
        null,
        null,
        null,
        null
    );
  }
}
//public record LedgerPosting(
//    LedgerAccount account,
//    LedgerEntryType entryType,
//    LedgerEntryDirection direction,
//    BigDecimal amount,
//    String description,
//    Long allocationId,
//    Long settlementId,
//    Long payoutId
//) {}
