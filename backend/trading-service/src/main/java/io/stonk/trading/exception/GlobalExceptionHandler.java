package io.stonk.trading.exception;

import io.stonk.trading.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TradeNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(TradeExecutionException.class)
    public ResponseEntity<ErrorResponse> handleExecution(TradeExecutionException ex, HttpServletRequest req) {
        log.error("Trade execution failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Trade Failed", ex.getMessage(), req);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(UserServiceUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUserUnauthorized(UserServiceUnauthorizedException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleDownstream(HttpClientErrorException ex, HttpServletRequest req) {
        log.error("Downstream service error: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return build(HttpStatus.valueOf(ex.getStatusCode().value()), "Service Error", ex.getResponseBodyAsString(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream().map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String e, String m, HttpServletRequest r) {
        return ResponseEntity.status(s).body(ErrorResponse.builder().status(s.value()).error(e).message(m).path(r.getRequestURI()).timestamp(LocalDateTime.now()).build());
    }
}
