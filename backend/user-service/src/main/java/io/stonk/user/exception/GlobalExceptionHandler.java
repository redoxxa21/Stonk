package io.stonk.user.exception;

import io.stonk.user.dto.ErrorResponse;
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
 * Centralised exception handler for all REST controllers.
 *
 * <p>Translates domain exceptions into consistent {@link ErrorResponse} payloads,
 * ensuring no stack traces or internal details leak to clients.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ────────────────────────────────────────────────
    // 404 — Not Found
    // ────────────────────────────────────────────────

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        log.warn("User not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    // ────────────────────────────────────────────────
    // 409 — Conflict
    // ────────────────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailConflict(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Email conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameConflict(
            UsernameAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Username conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    // ────────────────────────────────────────────────
    // 400 — Validation errors
    // ────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // ────────────────────────────────────────────────
    // 500 — Catch-all
    // ────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    // ────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildResponse(
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
