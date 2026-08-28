package com.benkih.estore.product.entity;

import com.benkih.estore.business.entity.Business;
//import com.benkih.estore.category.entity.Category;
import com.benkih.estore.common.enums.ProductCondition;
import com.benkih.estore.common.enums.ProductStatus;
import com.benkih.estore.common.enums.TaxCategory;
//import com.benkih.estore.image.entity.Image;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
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

    @Column(
        nullable = false,
        unique = true,
        updatable = false
    )
    private String slug;

    @NotBlank
    @Column(
        nullable = false,
        length = 255
    )
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 30
    )
    private ProductStatus status = ProductStatus.DRAFT;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "business_id",
        nullable = false
    )
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "tax_category",
        nullable = false,
        length = 30
    )
    private TaxCategory taxCategory = TaxCategory.STANDARD;

    @NotNull
    @PositiveOrZero
    @Column(
        nullable = false,
        precision = 10,
        scale = 3
    )
    private BigDecimal weightKg = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(
        precision = 10,
        scale = 2
    )
    private BigDecimal lengthCm;

    @PositiveOrZero
    @Column(
        precision = 10,
        scale = 2
    )
    private BigDecimal widthCm;

    @PositiveOrZero
    @Column(
        precision = 10,
        scale = 2
    )
    private BigDecimal heightCm;

    @Column(nullable = false)
    private boolean shippable = true;

    private String createdBy;

    private String updatedBy;

    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Image> images = new ArrayList<>();

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCondition condition = ProductCondition.NEW;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String model;

    @Column(length = 50)
    private String barcode;

    @Column(length = 14)
    private String gtin;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer warrantyMonths = 0;

    @Column(nullable = false)
    private boolean returnable = true;

    @Column(nullable = false)
    @PositiveOrZero
    private Integer returnWindowDays = 7;

    @Column
    private Instant archivedAt;

    public Product(
        String name,
        String brand,
        String description,
        Category category,
        Business business
    ) {
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.category = category;
        this.business = business;
    }

    @PrePersist
    public void onCreate() {
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

//package com.benkih.estore.product.entity;
//
//import com.benkih.estore.business.entity.Business;
//import com.benkih.estore.common.enums.Currency;
//import com.benkih.estore.common.enums.ProductStatus;
//import com.benkih.estore.common.enums.TaxCategory;
//import com.benkih.estore.inventory.entity.Inventory;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.DecimalMin;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.PositiveOrZero;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//@Table(name = "products")
//public class Product {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, updatable = false)
//    private String slug;
//
//    @Column(nullable = false, unique = true)
//    private String sku;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private ProductStatus status = ProductStatus.DRAFT;
//
//    private String brand;
//    private String name;
//    private String description;
//
////    @Column(nullable = false, precision = 19, scale = 2)
////    @NotNull
////    @DecimalMin("0.00")
////    private BigDecimal price;
//
//    private String createdBy;
//    private String updatedBy;
//
//    @Column(nullable = false,updatable = false)
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    @ManyToOne
//    @JoinColumn(name = "category_id")
//    private Category category;
//
//    @OneToMany(
//        mappedBy = "product",
//        cascade = CascadeType.ALL,
//        orphanRemoval = true
//        // fetch = FetchType.EAGER
//    )
//    private List<Image> images = new ArrayList<>();
//    //    private List<Image> images;
//
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
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "business_id", nullable = false)
//    private Business business;
//
////    @Enumerated(EnumType.STRING)
////    @Column(nullable = false, length = 3)
////    private Currency currency = Currency.NGN;
//
//    @Enumerated(EnumType.STRING)
//    @Column(
//        name = "tax_category",
//        nullable = false,
//        length = 30
//    )
//    private TaxCategory taxCategory = TaxCategory.STANDARD;
//
//    @Column(nullable = false, precision = 10, scale = 3)
//    @PositiveOrZero
//    private BigDecimal weightKg = BigDecimal.ZERO;
//
//    // Below is Product constructor
//    public Product(
//        String name,
//        String brand,
////        String sku,
////        ProductStatus status,
//        String description,
////        BigDecimal price,
//        Category category,
//        Business business
//    ) {
//        this.name = name;
//        this.brand = brand;
////        this.sku = sku;
////        this.status = status;
//        this.description = description;
////        this.price = price;
//        this.category = category;
//        this.business = business;
//    }
//
//    @PrePersist
//    public void onCreate() {
//        // this.slug = UUID.randomUUID().toString();
//
//        if (this.slug == null) {
//            this.slug = UUID.randomUUID().toString();
//        }
//        if (this.createdAt == null) {
//            this.createdAt = LocalDateTime.now();
//        }
//    }
//
//    @PreUpdate
//    public void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
//}
