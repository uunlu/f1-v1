package com.f1.seasonchampions.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @Getter
  private static class ApiError {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final Map<String, String> details;

    ApiError(final HttpStatus status, final String message) {
      this.timestamp = Instant.now();
      this.status = status.value();
      this.error = status.getReasonPhrase();
      this.message = message;
      this.details = new HashMap<>();
    }

    void addValidationError(final String field, final String detailMessage) {
      this.details.put(field, detailMessage);
    }
  }

  // For @Valid annotation validation failures
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationExceptions(
      final MethodArgumentNotValidException ex) {
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error");

    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              final String fieldName = ((FieldError) error).getField();
              final String errorMessage = error.getDefaultMessage();
              apiError.addValidationError(fieldName, errorMessage);
            });

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  // For @Validated validation failures
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(final ConstraintViolationException ex) {
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error");

    ex.getConstraintViolations()
        .forEach(
            violation -> {
              final String propertyPath = violation.getPropertyPath().toString();
              final String field =
                  propertyPath.contains(".")
                      ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1)
                      : propertyPath;
              final String detailMessage = violation.getMessage();
              apiError.addValidationError(field, detailMessage);
            });

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  // For logical validation errors
  @ExceptionHandler(InvalidInputException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(final InvalidInputException ex) {
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid argument");
    apiError.addValidationError("error", ex.getMessage());

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  // For missing required parameters
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingParams(
      final MissingServletRequestParameterException ex) {
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Missing parameter");
    apiError.addValidationError(ex.getParameterName(), "Parameter is required");

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  // For type conversion errors
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleTypeMismatch(final MethodArgumentTypeMismatchException ex) {
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Type conversion error");
    apiError.addValidationError(
        ex.getName(), "Should be of type " + ex.getRequiredType().getSimpleName());

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  // Fallback for any other exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleAllUncaughtException(final Exception ex) {
    final ApiError apiError =
        new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    apiError.addValidationError("error", ex.getMessage());

    return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
