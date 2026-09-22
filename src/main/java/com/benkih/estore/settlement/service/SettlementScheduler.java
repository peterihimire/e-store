package com.benkih.estore.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {
  private final SettlementEligibilityService eligibilityService;

  @Scheduled(cron = "${settlement.schedule}")
  public void processEligibleSettlements() {
    log.info("Starting settlement eligibility check");

    eligibilityService.processEligibleSettlements();

    log.info("Settlement eligibility check completed");
  }
}
