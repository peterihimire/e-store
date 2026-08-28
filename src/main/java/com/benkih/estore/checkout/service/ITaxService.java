package com.benkih.estore.checkout.service;

import com.benkih.estore.checkout.dto.TaxQuote;
import com.benkih.estore.order.entity.OrderItem;
import com.benkih.estore.user.entity.Address;

import java.math.BigDecimal;
import java.util.List;

public interface ITaxService {
  BigDecimal calculate(BigDecimal taxableAmount);

  TaxQuote quote(
      List<OrderItem> items,
      BigDecimal discount,
      Address address
  );
}
