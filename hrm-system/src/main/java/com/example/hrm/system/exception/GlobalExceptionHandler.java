package com.example.hrm.system.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════════════
    // ERROR RESPONSE DTO
    // ═══════════════════════════════════════════════════════════════

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<String> details;
    }

    // ═══════════════════════════════════════════════════════════════
    // AUTHENTICATION EXCEPTIONS (401, 403)
    // ═══════════════════════════════════════════════════════════════

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {
        log.warn("Bad credentials attempt: {}", request.getDescription(false));
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFound(
            UsernameNotFoundException ex,
            WebRequest request) {
        log.warn("Username not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwt(
            ExpiredJwtException ex,
            WebRequest request) {
        log.warn("Expired JWT token: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Token has expired — please log in again",
                request);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Object> handleMalformedJwt(
            MalformedJwtException ex,
            WebRequest request) {
        log.warn("Malformed JWT token: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid token format",
                request);
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATION EXCEPTIONS (400)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Handle @Valid annotation validation errors
     * Collects all field validation errors and returns them
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return buildErrorResponseWithDetails(
                HttpStatus.BAD_REQUEST,
                "Input validation failed. Please check the details.",
                request,
                details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request);
    }

    // ═══════════════════════════════════════════════════════════════
    // RESOURCE EXCEPTIONS (404)
    // ═══════════════════════════════════════════════════════════════

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request);
    }

    // ═══════════════════════════════════════════════════════════════
    // BUSINESS LOGIC EXCEPTIONS (409 CONFLICT)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Handle duplicate record exceptions
     * When trying to create a record that already exists
     */
    @ExceptionHandler(DuplicateRecordException.class)
    public ResponseEntity<Object> handleDuplicateRecord(
            DuplicateRecordException ex,
            WebRequest request) {
        log.warn("Duplicate record: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request);
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACK EXCEPTION HANDLER (500)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Catch-all handler for any unhandled exceptions
     * Logs the full stack trace for debugging
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(
            Exception ex,
            WebRequest request) {
        // Log full stack trace — this is critical for debugging
        log.error("Unhandled exception at {}: {}",
                request.getDescription(false),
                ex.getMessage(),
                ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.",
                request);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Build error response without details
     */
    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status,
            String message,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Build error response with validation details
     */
    private ResponseEntity<Object> buildErrorResponseWithDetails(
            HttpStatus status,
            String message,
            WebRequest request,
            List<String> details) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .details(details)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Build error response using LinkedHashMap for backward compatibility
     * (Kept for reference - use ErrorResponse DTO instead)
     */
    private ResponseEntity<Object> buildLegacyErrorResponse(
            HttpStatus status,
            String message,
            WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
}