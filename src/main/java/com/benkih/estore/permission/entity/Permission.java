package com.benkih.estore.permission.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.role.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
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
        name = "permissions",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_permission_resource_action",
                    columnNames = {"resource", "action"})
        },
        indexes = {
            @Index(name = "idx_permissions_name", columnList = "name"),
            @Index(name = "idx_permissions_resource_action", columnList = "resource, action")
        })
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private String resource;

    @Column(nullable = false)
    private String action;

    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}
