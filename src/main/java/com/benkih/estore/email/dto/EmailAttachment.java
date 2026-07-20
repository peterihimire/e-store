package com.benkih.estore.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {
  private String filename;
  private String contentType;
  private byte[] content;
}
