package com.benkih.estore.product.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.AttributeType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "category_attributes",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_category_attribute",
                    columnNames = {"category_id", "name"})
        })
public class CategoryAttribute extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttributeType type = AttributeType.TEXT;

    @Column(nullable = false)
    private boolean required = false;

    @Column(nullable = false)
    private boolean variantAttribute = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeValue> values = new ArrayList<>();

    public CategoryAttribute(Category category, String name, AttributeType type) {
        this.category = category;
        this.name = name;
        this.type = type;
    }
}
