package com.benkih.estore.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {
  private String slug;
  private BigDecimal totalAmount;
  private List<CartItemResponseDto> items;
}
