package com.benkih.estore.category.entity;

import com.benkih.estore.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String slug;

    private String name;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
