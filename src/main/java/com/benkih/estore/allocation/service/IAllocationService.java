package com.benkih.estore.allocation.service;

import com.benkih.estore.allocation.entity.Allocation;
import com.benkih.estore.payment.entity.Payment;

import java.util.List;

public interface IAllocationService {

  List<Allocation> allocatePayment(Payment payment);
}
