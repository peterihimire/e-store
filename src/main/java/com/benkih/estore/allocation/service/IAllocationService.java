package com.benkih.estore.allocation.service;

import com.benkih.estore.payment.entity.Payment;

public interface IAllocationService {

  void allocatePayment(Payment payment);
}
