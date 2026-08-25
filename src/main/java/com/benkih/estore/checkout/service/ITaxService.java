package com.benkih.estore.checkout.service;

import java.math.BigDecimal;

public interface ITaxService {
  BigDecimal calculate(BigDecimal taxableAmount);
}
