package com.benkih.estore.role.entity;

import com.benkih.estore.business.entity.Business;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.permission.entity.Permission;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // use this when you create your own arg constructor manually
@Entity
@Table(
        name = "roles",
        indexes = {
            @Index(name = "idx_roles_slug", columnList = "slug"),
            @Index(name = "idx_roles_name", columnList = "name"),
            @Index(name = "idx_roles_business_id", columnList = "business_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_role_business_name",
                    columnNames = {"business_id", "name"})
        })
public class Role extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean systemRole = false;

    @Column(nullable = false)
    private boolean active = true;

    /*
     * System roles are associated with users through this relationship.
     *
     * Business roles should NOT be assigned here.
     * Business roles are assigned through BusinessMember.
     */
    @ManyToMany(mappedBy = "roles")
    private Collection<User> users =
            new HashSet<>(); // using collection here allows the user to switch between Set and

    // ArrayList

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    /*
     * NULL  -> system role
     * NOT NULL -> business-specific role
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    public Role(String name) { // manual arg constructor
        this.name = name;
    }
}
