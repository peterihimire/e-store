package com.benkih.estore.business.entity;

import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.enums.BusinessAddressType;
import com.benkih.estore.common.enums.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "business_addresses")
public class BusinessAddress extends AuditableEntity {

    @Column(nullable = false)
    private String addressName;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country = Country.NIGERIA;

    @Column(nullable = false)
    private String addressLine1;

    private String addressLine2;

    @Column(nullable = false)
    private String city;

    private String state;

    private String postalCode;

    @Column(nullable = false)
    private boolean defaultAddress = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BusinessAddressType type;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
