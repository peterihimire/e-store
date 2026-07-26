package com.benkih.estore.order.dto.response;

import com.benkih.estore.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusResponseDto {
  private OrderResponseDto order;
  private List<OrderStatus> allowedNextStatuses;
}
