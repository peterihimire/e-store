package com.benkih.estore.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartProductResponseDto {
  private String slug;

  private String name;

  private String imageUrl;
}
