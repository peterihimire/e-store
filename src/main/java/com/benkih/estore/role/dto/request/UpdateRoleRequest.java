package com.benkih.estore.role.dto.request;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    private String name;

    private String description;

    private Set<String> permissionSlugs;

    private Boolean active;
}
