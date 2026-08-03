package com.benkih.estore.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponseDto {

  private String slug;

  private String firstName;

  private String lastName;

  private String email;
}