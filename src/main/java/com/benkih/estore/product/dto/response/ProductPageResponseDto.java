package com.benkih.estore.product.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPageResponseDto {
    private List<ProductResponseDto> products;
    private long total;
    private int totalPages;
    private int page;
    private int limit;
}
