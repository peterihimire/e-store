package com.benkih.estore.permission.dto.response;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {
  private String slug;

  private String name;

  private String description;

  private String resource;

  private String action;
}
