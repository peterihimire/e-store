package com.benkih.estore.settlement.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.allocation.repository.AllocationRepository;
import com.benkih.estore.order.entity.Order;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.settlement.dto.response.SettlementGroup;
import com.benkih.estore.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SettlementEligibilityService {
  private final AllocationRepository allocationRepository;
  private final SettlementService settlementService;


  public void processEligibleSettlements() {
    Instant now = Instant.now();

    List<Allocation> allocations = allocationRepository.findDeliveredAndUnsettled();

    List<Allocation> eligibleAllocations = allocations.stream()
        .filter(allocation -> isEligibleForSettlement(allocation, now))
        .toList();

    if (eligibleAllocations.isEmpty()) {
      log.info("No allocations eligible for settlement");
      return;
    }

    Map<SettlementGroup, List<Allocation>> groups =
        eligibleAllocations.stream()
            .collect(Collectors.groupingBy(
                allocation -> new SettlementGroup(
                    allocation.getBusiness().getId(),
                    allocation.getCurrency()
                )
            ));

    for (List<Allocation> group : groups.values()) {
      Allocation first = group.get(0);

      Settlement settlement = settlementService.createSettlement(
              first.getBusiness(),
              first.getCurrency(),
              group,
              determinePeriodStart(group),
              now
          );

      settlementService.releaseSettlement(settlement);
    }
  }


  private Instant determinePeriodStart(
      List<Allocation> allocations
  ) {
    return allocations.stream()
        .map(allocation ->
            allocation.getOrderItem()
                .getOrder()
                .getOrderDate()
        )
        .min(Instant::compareTo)
        .orElse(Instant.now());
  }

  private boolean isEligibleForSettlement(
      Allocation allocation,
      Instant now
  ) {
    Order order = allocation.getOrderItem().getOrder();
    OrderItem orderItem = allocation.getOrderItem();

    if (order.getDeliveredAt() == null) {
      return false;
    }

    if (!orderItem.isReturnable()) {
      return true;
    }

    Instant eligibleAt = order.getDeliveredAt()
        .plus(Duration.ofDays(orderItem.getReturnWindowDays()));

    return !now.isBefore(eligibleAt);
  }
}
