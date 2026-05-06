package io.stonk.market.exception;

import io.stonk.market.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(StockNotFoundException ex, HttpServletRequest req) {
        log.warn("Stock not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .status(status.value()).error(error).message(message)
                .path(req.getRequestURI()).timestamp(LocalDateTime.now()).build());
    }
}
