package com.benkih.estore.order.dto.response;

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
public class OrderItemResponseDto { // This helps in the formation of the cart response. When the circular injection happens, instead of using @JsonIgnore on the
  private String slug;

  private OrderItemProductResponseDto product;

  private String variantSlug;

  private String sku;

  private List<VariantAttributeResponseDto> selectedAttributes;

  private int quantity;

  private BigDecimal price;

  private BigDecimal subtotal;

  private BigDecimal discountAmount;

  private BigDecimal taxRate;

  private BigDecimal taxAmount;

}
