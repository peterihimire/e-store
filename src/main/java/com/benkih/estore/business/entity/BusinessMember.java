package com.benkih.estore.business.entity;

import com.benkih.estore.business.enums.BusinessMemberRole;
import com.benkih.estore.business.enums.MemberStatus;
import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.department.entity.Department;
import com.benkih.estore.role.entity.Role;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "business_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_business_user",
            columnNames = {"business_id", "user_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class BusinessMember extends BaseEntity {


  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

//  @Enumerated(EnumType.STRING)
//  @Column(nullable = false)
//  private BusinessMemberRole role;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatus status = MemberStatus.ACTIVE;
}
