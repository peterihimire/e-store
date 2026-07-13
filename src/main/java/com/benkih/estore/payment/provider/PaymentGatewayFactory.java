package com.benkih.estore.payment.provider;

import com.benkih.estore.common.enums.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayFactory {

  private final Map<PaymentProvider, PaymentGateway> gateways;

  public PaymentGatewayFactory(List<PaymentGateway> providers) {

    this.gateways = providers.stream()
        .collect(Collectors.toMap(
            PaymentGateway::supports,
            Function.identity()
        ));
  }

  public PaymentGateway get(PaymentProvider provider) {

    PaymentGateway gateway = gateways.get(provider);

    if (gateway == null) {
      throw new IllegalArgumentException(
          "Unsupported payment provider: " + provider
      );
    }

    return gateway;
  }
}