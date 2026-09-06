package com.benkih.estore.settlement.repository;

import com.benkih.estore.settlement.entity.Settlement;
import com.benkih.estore.settlement.entity.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementItemRepository extends JpaRepository<SettlementItem,
    Long> {
}
