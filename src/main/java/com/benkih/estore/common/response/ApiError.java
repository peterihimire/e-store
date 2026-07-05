package com.benkih.estore.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiError {
  private int status;
  private String error;
  private String message;
  private String path;
  private Instant timestamp;
}
