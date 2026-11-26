package com.benkih.estore.department.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "departments",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_department_business_name",
                    columnNames = {"business_id", "name"})
        },
        indexes = {
            @Index(name = "idx_departments_business_id", columnList = "business_id"),
            @Index(name = "idx_departments_business_name", columnList = "business_id, name")
        })
public class Department extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToMany(mappedBy = "departments")
    private Set<User> users = new HashSet<>();
}
