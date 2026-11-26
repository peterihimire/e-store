package com.benkih.estore.order.service;

import com.benkih.estore.order.entity.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPricingService {

    public BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {
        return orderItemList.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal taxAmount,
            BigDecimal discountAmount) {
        return subtotal.subtract(discountAmount)
                .add(taxAmount)
                .add(shippingFee)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
