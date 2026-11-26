package com.benkih.estore.product.dto.request;

import com.benkih.estore.product.entity.Brand;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateProductRequest {
    private Long id;
    private String slug;
    private String name;
    private Brand brand;
    private String description;
    private BigDecimal price;
    private Integer inventory; // use Integer rather than int, int will default to 0

    private String categoryName;
    //  private Category category;
}
