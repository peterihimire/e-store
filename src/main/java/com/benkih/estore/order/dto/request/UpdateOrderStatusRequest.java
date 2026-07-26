package com.benkih.estore.order.dto.request;

import com.benkih.estore.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusRequest {
  private OrderStatus status;
}
