package com.benkih.estore.payout.service;

import com.benkih.estore.payout.enums.PayoutProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payout")
@Getter
@Setter
public class PayoutProperties {
  private PayoutProvider provider;
}
