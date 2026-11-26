package com.benkih.estore.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    private String slug;
    private String name;
    private String parentSlug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private boolean active;
}
