package com.benkih.estore.common.exceptions;

import com.benkih.estore.common.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ApiError> buildError(
      HttpStatus status,
      String message,
      HttpServletRequest request
  ) {
    ApiError error = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()
    );

    return ResponseEntity.status(status).body(error);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request) {

    return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(AlreadyExistsException.class)
  public ResponseEntity<ApiError> handleAlreadyExists(
      AlreadyExistsException ex,
      HttpServletRequest request) {

    return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(
      BadRequestException ex,
      HttpServletRequest request) {

    return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiError> handleUnauthorized(
      UnauthorizedException ex,
      HttpServletRequest request) {

    return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(
      ForbiddenException ex,
      HttpServletRequest request) {

    return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {

    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("Validation failed");

    return buildError(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingRequestParameter(
      MissingServletRequestParameterException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.BAD_REQUEST,
        "Request parameter '" + ex.getParameterName() + "' is required",
        request
    );
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleInvalidJson(
      HttpMessageNotReadableException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.BAD_REQUEST,
        "Malformed JSON request",
        request
    );
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(
      ConstraintViolationException ex,
      HttpServletRequest request) {

    String message = ex.getConstraintViolations()
        .stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .findFirst()
        .orElse("Validation failed");

    return buildError(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.BAD_REQUEST,
        "Invalid value for parameter '" + ex.getName() + "'",
        request
    );
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiError> handleNoResource(
      NoResourceFoundException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.NOT_FOUND,
        "Endpoint not found",
        request
    );
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpServletRequest request) {

    return buildError(
        HttpStatus.METHOD_NOT_ALLOWED,
        "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
        request
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleException(
      Exception ex,
      HttpServletRequest request) {

    log.error("Unexpected error occurred", ex);

    return buildError(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getClass().getSimpleName() + ": " + ex.getMessage(),
//        "An unexpected error occurred",
        request
    );
  }
}

//package com.benkih.estore.common.exceptions;
//
//import com.benkih.estore.common.response.ApiError;
//import jakarta.validation.ConstraintViolationException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.MissingServletRequestParameterException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
//
//  @ExceptionHandler(ResourceNotFoundException.class)
//  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
//
//    return ResponseEntity.status(HttpStatus.NOT_FOUND)
//        .body(new ApiError(
//            HttpStatus.NOT_FOUND.value(),
//            ex.getMessage()
//        ));
//  }
//
//  @ExceptionHandler(AlreadyExistsException.class)
//  public ResponseEntity<ApiError> handleAlreadyExists(AlreadyExistsException ex) {
//
//    return ResponseEntity.status(HttpStatus.CONFLICT)
//        .body(new ApiError(
//            HttpStatus.CONFLICT.value(),
//            ex.getMessage()
//        ));
//  }
//
//  @ExceptionHandler(BadRequestException.class)
//  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
//
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//        .body(new ApiError(
//            HttpStatus.BAD_REQUEST.value(),
//            ex.getMessage()
//        ));
//  }
//
//  @ExceptionHandler(UnauthorizedException.class)
//  public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
//
//    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//        .body(new ApiError(
//            HttpStatus.UNAUTHORIZED.value(),
//            ex.getMessage()
//        ));
//  }
//
//  @ExceptionHandler(ForbiddenException.class)
//  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex) {
//
//    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//        .body(new ApiError(
//            HttpStatus.FORBIDDEN.value(),
//            ex.getMessage()
//        ));
//  }
//
//  @ExceptionHandler(MethodArgumentNotValidException.class)
//  public ResponseEntity<ApiError> handleValidationExceptions(
//      MethodArgumentNotValidException ex) {
//    String message = ex.getBindingResult()
//        .getFieldErrors()
//        .stream()
//        .map(error -> error.getField() + ": " + error.getDefaultMessage())
//        .findFirst()
//        .orElse("Validation failed");
//
//    return ResponseEntity.badRequest()
//        .body(new ApiError(
//            HttpStatus.BAD_REQUEST.value(),
//            message
//        ));
//  }
//
//  @ExceptionHandler(MissingServletRequestParameterException.class)
//
//  public ResponseEntity<ApiError> handleMissingRequestParameter(
//
//      MissingServletRequestParameterException ex) {
//
//    return ResponseEntity.badRequest()
//
//        .body(new ApiError(
//
//            HttpStatus.BAD_REQUEST.value(),
//
//            ex.getParameterName() + " is required"
//
//        ));
//
//  }
//
//  @ExceptionHandler(HttpMessageNotReadableException.class)
//
//  public ResponseEntity<ApiError> handleInvalidJson(
//
//      HttpMessageNotReadableException ex) {
//
//    return ResponseEntity.badRequest()
//
//        .body(new ApiError(
//
//            HttpStatus.BAD_REQUEST.value(),
//
//            "Malformed JSON request"
//
//        ));
//
//  }
//
//  @ExceptionHandler(ConstraintViolationException.class)
//
//  public ResponseEntity<ApiError> handleConstraintViolation(
//
//      ConstraintViolationException ex) {
//
//    String message = ex.getConstraintViolations()
//
//        .stream()
//
//        .map(v -> v.getMessage())
//
//        .findFirst()
//
//        .orElse("Validation failed");
//
//    return ResponseEntity.badRequest()
//
//        .body(new ApiError(
//
//            HttpStatus.BAD_REQUEST.value(),
//
//            message
//
//        ));
//
//  }
//
//  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
//
//  public ResponseEntity<ApiError> handleTypeMismatch(
//
//      MethodArgumentTypeMismatchException ex) {
//
//    return ResponseEntity.badRequest()
//
//        .body(new ApiError(
//
//            HttpStatus.BAD_REQUEST.value(),
//
//            "Invalid value for '" + ex.getName() + "'"
//
//        ));
//
//  }
//
//  @ExceptionHandler(Exception.class)
//  public ResponseEntity<ApiError> handleException(Exception ex) {
//    log.error("Unexpected error occurred", ex);
//    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//        .body(new ApiError(
//            HttpStatus.INTERNAL_SERVER_ERROR.value(),
//            "An unexpected error occurred"
//        ));
//  }
//}