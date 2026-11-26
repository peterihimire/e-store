package com.benkih.estore.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
