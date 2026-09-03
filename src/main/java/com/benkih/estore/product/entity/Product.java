package com.benkih.estore.product.entity;

import com.benkih.estore.business.entity.Business;
//import com.benkih.estore.category.entity.Category;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
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
public class Product extends AuditableEntity {

    @NotBlank
    @Column(
        nullable = false,
        length = 255
    )
    private String name;

//    @Column(length = 100)
//    private String brand;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

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


    @Column(nullable = false)
    private boolean shippable = true;

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
//    @OrderBy("displayOrder ASC")
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCondition condition = ProductCondition.NEW;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String model;


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
        Brand brand,
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
}
