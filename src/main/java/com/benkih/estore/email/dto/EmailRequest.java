package com.benkih.estore.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
  private String to;
  private String subject;
  private String html;
  private String text;
  private List<File> attachments;

}
