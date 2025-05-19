package com.f1.seasonchampions.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Create a standard error response structure
    @Getter
    private static class ApiError {
        private final Instant timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final Map<String, String> details;

        public ApiError(HttpStatus status, String message) {
            this.timestamp = Instant.now();
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.details = new HashMap<>();
        }

        // Add a validation error detail
        public void addValidationError(String field, String message) {
            details.put(field, message);
        }
    }

    // For @Valid annotation validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error");

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            apiError.addValidationError(fieldName, errorMessage);
        });

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // For @Validated validation failures
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error");

        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String field = propertyPath.contains(".") ?
                    propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;
            String message = violation.getMessage();
            apiError.addValidationError(field, message);
        });

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // For logical validation errors
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(InvalidInputException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid argument");
        apiError.addValidationError("error", ex.getMessage());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // For missing required parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Missing parameter");
        apiError.addValidationError(ex.getParameterName(), "Parameter is required");

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // For type conversion errors
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Type conversion error");
        apiError.addValidationError(ex.getName(), "Should be of type " +
                ex.getRequiredType().getSimpleName());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // Fallback for any other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUncaughtException(Exception ex) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        apiError.addValidationError("error", ex.getMessage());

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}