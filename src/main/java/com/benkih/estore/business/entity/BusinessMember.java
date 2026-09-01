package com.benkih.estore.business.entity;

import com.benkih.estore.business.enums.BusinessMemberRole;
import com.benkih.estore.business.enums.MemberStatus;
import com.benkih.estore.common.entity.AuditableEntity;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "business_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_business_member_user",
            columnNames = {"user_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class BusinessMember extends AuditableEntity {


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,unique = true)
  private User user;

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "business_member_roles",
    joinColumns = @JoinColumn(name = "business_member_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "business_member_departments",
    joinColumns = @JoinColumn(name = "business_member_id"),
    inverseJoinColumns = @JoinColumn(name = "department_id")
)
private Set<Department> departments = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatus status = MemberStatus.ACTIVE;
}
