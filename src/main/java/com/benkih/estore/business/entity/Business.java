package com.benkih.estore.business.entity;

import com.benkih.estore.business.enums.BusinessStatus;
import com.benkih.estore.business.enums.BusinessType;
import com.benkih.estore.common.entity.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "businesses")
public class Business extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    private String legalName;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String logoUrl;
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessStatus status = BusinessStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType type = BusinessType.COMPANY;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessAddress> addresses = new ArrayList<>();
}
