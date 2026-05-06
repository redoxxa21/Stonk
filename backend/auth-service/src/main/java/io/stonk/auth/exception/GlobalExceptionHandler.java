package io.stonk.auth.exception;

import io.stonk.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralised exception handler — maps all domain and validation exceptions
 * to consistent {@link ErrorResponse} payloads.
 *
 * <p>No stack traces or internal details are ever returned to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ────────────────────────────────────────────────
    // 409 — Conflict
    // ────────────────────────────────────────────────

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {

        log.warn("Registration conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    // ────────────────────────────────────────────────
    // 401 — Unauthorised
    // ────────────────────────────────────────────────

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {

        // Log without details to avoid leaking info to logs
        log.warn("Failed login attempt from {}", request.getRemoteAddr());
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
    }

    // ────────────────────────────────────────────────
    // 400 — Validation errors
    // ────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", message);
        return build(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // ────────────────────────────────────────────────
    // 500 — Catch-all
    // ────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    // ────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
