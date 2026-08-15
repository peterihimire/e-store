package com.benkih.estore.security.tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantContext {
  private final ThreadLocal<Long> businessId = new ThreadLocal<>();

  public void setBusinessId(Long id) {
    businessId.set(id);
  }

  public Long getBusinessId() {
    Long id = businessId.get();

    if (id == null) {
      throw new IllegalStateException("No business context available");
    }

    return id;
  }

  public void clear() {
    businessId.remove();
  }
}
