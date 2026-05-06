package io.stonk.portfolio.exception;

import io.stonk.portfolio.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HoldingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(HoldingNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(InsufficientHoldingException.class)
    public ResponseEntity<ErrorResponse> handleInsufficient(InsufficientHoldingException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(InsufficientWalletBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientWallet(InsufficientWalletBalanceException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(WalletServiceUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleWalletUnauthorized(WalletServiceUnauthorizedException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req);
    }

    @ExceptionHandler(WalletOperationException.class)
    public ResponseEntity<ErrorResponse> handleWalletOperation(WalletOperationException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
    }

    @ExceptionHandler(UserServiceUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUserUnauthorized(UserServiceUnauthorizedException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req);
    }

    @ExceptionHandler(UserAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleUserAccessDenied(UserAccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Bad Request", msg, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String e, String m, HttpServletRequest r) {
        return ResponseEntity.status(s).body(ErrorResponse.builder().status(s.value()).error(e).message(m)
                .path(r.getRequestURI()).timestamp(LocalDateTime.now()).build());
    }
}
