package com.benkih.estore.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {
    private String slug;
    private BigDecimal totalAmount;
    private List<CartItemResponseDto> items;
}
