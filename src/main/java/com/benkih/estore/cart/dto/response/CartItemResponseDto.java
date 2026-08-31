package com.benkih.estore.cart.dto.response;

import com.benkih.estore.product.dto.response.ProductResponseDto;
import com.benkih.estore.product.dto.response.VariantAttributeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDto { // This helps in the formation of the cart response. When the circular injection happens, instead of using @JsonIgnore on the
  private String slug;

  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;

  private String variantSlug;
  private String sku;

  private ProductResponseDto product;

  private List<VariantAttributeResponseDto> selectedAttributes;
}
