package com.benkih.estore.common.exceptions;

import com.benkih.estore.common.response.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        ));
  }

  @ExceptionHandler(AlreadyExistsException.class)
  public ResponseEntity<ApiError> handleAlreadyExists(AlreadyExistsException ex) {

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiError(
            HttpStatus.CONFLICT.value(),
            ex.getMessage()
        ));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        ));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage()
        ));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex) {

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ApiError(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage()
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleException(Exception ex) {
    log.error("Unexpected error occurred", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred"
        ));
  }
}