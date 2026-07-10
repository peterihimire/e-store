package com.benkih.estore.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {
  private String imageSlug;
  private String imageName;
  private String downloadUrl;
}
