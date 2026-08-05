package com.benkih.estore.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AuditableEntity extends BaseEntity {

  protected String createdBy;
  protected String updatedBy;
}
