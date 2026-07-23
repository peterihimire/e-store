package com.benkih.estore.product.entity;

import com.benkih.estore.common.enums.ProductStatus;
import com.benkih.estore.inventory.entity.Inventory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String slug;

    @Column(nullable = false, unique = true)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    private String brand;
    private String name;
    private String description;
    private BigDecimal price;

    private String createdBy;
    private String updatedBy;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true
        // fetch = FetchType.EAGER
    )
    private List<Image> images = new ArrayList<>();
    //    private List<Image> images;

//    @OneToOne(
//        mappedBy = "product", //  Inverse side of the relationship
//        cascade = CascadeType.ALL,
//        orphanRemoval = true,
//        fetch = FetchType.LAZY
//        // JPA default is EAGER for @OneToOne.
//        // However, on the inverse (mappedBy) side,
//        // Hibernate cannot lazily load this association
//        // without bytecode enhancement, so it may still
//        // behave as EAGER.that's why we have the warning
//    )
//    private Inventory inventory;

    // Below is Product constructor
    public Product(
        String name,
        String brand,
//        String sku,
//        ProductStatus status,
        String description,
        BigDecimal price,
        Category category
    ) {
        this.name = name;
        this.brand = brand;
//        this.sku = sku;
//        this.status = status;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    @PrePersist
    public void onCreate() {
        // this.slug = UUID.randomUUID().toString();

        if (this.slug == null) {
            this.slug = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
