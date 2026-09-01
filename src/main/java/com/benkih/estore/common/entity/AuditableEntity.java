package com.benkih.estore.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity extends BaseEntity {

  @Column(name = "created_by", updatable = false)
  protected String createdBy;

  @Column(name = "updated_by")
  protected String updatedBy;
}
