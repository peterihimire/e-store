package com.benkih.estore.product.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_category_parent_name",
            columnNames = {
                "parent_id",
                "name"
            }
        )
    }
)
public class Category {

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
        length = 150
    )
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private String createdBy;

    private String updatedBy;

    @OneToMany(
        mappedBy = "category",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<CategoryAttribute> attributes = new ArrayList<>();

    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Category(String name) {
        this.name = name;
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
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@Entity
//@Table(name = "categories",
//    uniqueConstraints = {
//        @UniqueConstraint(
//            name = "uk_category_business_parent_name",
//            columnNames = {
//                "business_id",
//                "parent_id",
//                "name"
//            }
//        )
//    })
////@AllArgsConstructor
////@Builder
//public class Category {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, updatable = false)
//    private String slug;
//
//    @Column(nullable = false)
//    private String name;
//
//    private String createdBy;
//    private String updatedBy;
//
//    @Column(nullable = false,updatable = false)
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    @JsonIgnore // using it to brake the product category loop from continuing calling itself, however using DTO is better
//    @OneToMany(mappedBy = "category")
//    private List<Product> products;
//
//    // Category constructor
//    public Category(String name) {
//        this.name = name;
//    }
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "business_id", nullable = false)
//    private Business business;
//
//    @PrePersist
//    public void onCreate() {
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
