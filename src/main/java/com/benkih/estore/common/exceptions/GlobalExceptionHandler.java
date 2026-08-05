package com.benkih.estore.common.exceptions;

import com.benkih.estore.common.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;


import javax.naming.AuthenticationException;
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
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        Instant.now()
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

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(
      BadCredentialsException ex,
      HttpServletRequest request) {

    log.warn("Bad credentials for request {}: {}", request.getRequestURI(), ex.getMessage());

    return buildError(
        HttpStatus.BAD_REQUEST,
        "Invalid email or password",
        request
    );
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuthenticationException(
      AuthenticationException ex,
      HttpServletRequest request) {

    //    String message;
    //    if (ex instanceof org.springframework.security.authentication.BadCredentialsException) {
    //      message = "Invalid email or password";
    //    } else if (ex instanceof org.springframework.security.authentication.DisabledException) {
    //      message = "Your account has been disabled. Please contact support.";
    //    } else if (ex instanceof org.springframework.security.authentication.LockedException) {
    //      message = "Your account has been locked. Please contact support.";
    //    } else {
    //      message = "Authentication failed: " + ex.getMessage();
    //    }

    return buildError(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage(), request);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(
      AuthorizationDeniedException ex,
      HttpServletRequest request) {

    String userRoles = "No roles found";
    String userEmail = "Unknown user";

    try {
      var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated()) {
        userEmail = authentication.getName();
        userRoles = authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .collect(java.util.stream.Collectors.joining(", "));

        // If roles are empty, try to get them another way
        if (userRoles.isEmpty()) {
          // Check if we have user details
          Object principal = authentication.getPrincipal();
          if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            userRoles = ((org.springframework.security.core.userdetails.UserDetails) principal)
                .getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(java.util.stream.Collectors.joining(", "));
          }
        }
      }
    } catch (Exception e) {
      log.warn("Could not retrieve user roles: {}", e.getMessage());
    }

    String message = String.format(
        "Access Denied: User '%s' does not have the required permissions to access this resource. ",
        userEmail,
        userRoles.isEmpty() ? "None" : userRoles
    );

    log.warn("Access denied for user {} to endpoint {}. Required permissions...",
        userEmail, request.getRequestURI());

    return buildError(HttpStatus.FORBIDDEN, message, request);
  }

  // In GlobalExceptionHandler
  @ExceptionHandler(PaymentException.class)
  public ResponseEntity<ApiError> handlePaymentException(
      PaymentException ex,
      HttpServletRequest request) {

    log.warn("Payment error: {}", ex.getMessage());

    String message = "Payment processing failed: " + ex.getMessage();
    if (ex.getCause() != null) {
      log.debug("Payment exception cause: ", ex.getCause());
    }

    return buildError(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(PaymentGatewayException.class)
  public ResponseEntity<ApiError> handlePaymentGatewayException(
      PaymentGatewayException ex,
      HttpServletRequest request) {

    log.error("Payment gateway error: {}", ex.getMessage(), ex);

    String message = "Payment gateway is currently unavailable. Please try again later.";
    if (ex.getCause() != null) {
      log.debug("Gateway exception cause: ", ex.getCause());
    }

    return buildError(HttpStatus.BAD_GATEWAY, message, request);
  }

  @ExceptionHandler(DuplicatePaymentException.class)
  public ResponseEntity<ApiError> handleDuplicatePaymentException(
      DuplicatePaymentException ex,
      HttpServletRequest request) {

    log.warn("Duplicate payment detected: {}", ex.getMessage());

    String message = "Duplicate payment detected: " + ex.getMessage();

    return buildError(HttpStatus.CONFLICT, message, request);
  }

  @ExceptionHandler(PaymentVerificationException.class)
  public ResponseEntity<ApiError> handlePaymentVerificationException(
      PaymentVerificationException ex,
      HttpServletRequest request) {

    log.error("Payment verification failed: {}", ex.getMessage(), ex);

    String message = "Payment verification failed: " + ex.getMessage();

    return buildError(HttpStatus.BAD_REQUEST, message, request);
  }

  @ExceptionHandler(PaymentTimeoutException.class)
  public ResponseEntity<ApiError> handlePaymentTimeoutException(
      PaymentTimeoutException ex,
      HttpServletRequest request) {

    log.error("Payment timeout: {}", ex.getMessage(), ex);

    String message = "Payment request timed out. Please try again.";

    return buildError(HttpStatus.REQUEST_TIMEOUT, message, request);
  }

  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ApiError> handleInsufficientStockException(
      InsufficientStockException ex,
      HttpServletRequest request) {

    log.error("Insufficient stock: {}", ex.getMessage(), ex);

    String message = "Insufficient stock: " + ex.getMessage();

    return buildError(HttpStatus.CONFLICT, message, request);
  }

  @ExceptionHandler(InvalidInventoryQuantityException.class)
  public ResponseEntity<ApiError> handleInvalidInventoryQuantityException(
      InvalidInventoryQuantityException ex,
      HttpServletRequest request) {

    log.error("Invalid inventory quantity: {}", ex.getMessage(), ex);

    String message = "Invalid inventory quantity: " + ex.getMessage();

    return buildError(HttpStatus.BAD_REQUEST, message, request);
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