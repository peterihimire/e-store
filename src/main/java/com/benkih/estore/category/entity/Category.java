package com.benkih.estore.category.entity;

import com.benkih.estore.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String createdBy;
    private String updatedBy;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Product> products;

    // Category constructor
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
