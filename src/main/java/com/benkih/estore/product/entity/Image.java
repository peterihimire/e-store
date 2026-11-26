package com.benkih.estore.product.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "images")
public class Image extends AuditableEntity {

    private String fileName;
    private String fileType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    private String downloadUrl;

    @Column(nullable = false)
    private String storageKey;

    @Column(length = 255)
    private String altText;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private boolean primaryImage = false;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
