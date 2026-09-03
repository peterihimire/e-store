package com.benkih.estore.order.dto.response;

import com.benkih.estore.product.dto.response.VariantAttributeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemProductResponseDto {

  private String slug;
  private String name;
  private String brand;
  private String category;
  private String imageUrl;

}
