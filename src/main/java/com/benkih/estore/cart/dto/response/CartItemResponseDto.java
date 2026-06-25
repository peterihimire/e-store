package com.benkih.estore.cart.dto.response;

import com.benkih.estore.product.dto.response.ProductResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDto { // This helps in the formation of the cart response. When the circular injection happens, instead of using @JsonIgnore on the
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;
  private ProductResponseDto product;
}
