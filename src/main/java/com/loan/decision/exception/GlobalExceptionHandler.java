package com.loan.decision.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler that intercepts all exceptions thrown by controllers
 * and returns a standardised error response with timestamp, status, error, message,
 * path, and traceId.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Inner response record
    // -------------------------------------------------------------------------

    @Getter
    @Builder
    public static class ErrorResponse {
        private final String timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final String path;
        private final String traceId;
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ErrorResponse buildError(HttpStatus httpStatus, String message, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .traceId(UUID.randomUUID().toString().substring(0, 8))
                .build();
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(buildError(status, message, request));
    }

    // -------------------------------------------------------------------------
    // 404 – Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(LoanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLoanNotFound(LoanNotFoundException ex,
                                                             HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex,
                                                             HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 409 – Conflict
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidLoanStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLoanState(InvalidLoanStateException ex,
                                                                  HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateApplication(DuplicateApplicationException ex,
                                                                      HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 400 – Bad Request
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConfigValidationException.class)
    public ResponseEntity<ErrorResponse> handleConfigValidation(ConfigValidationException ex,
                                                                  HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                        HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return respond(HttpStatus.BAD_REQUEST, message, request);
    }

    // -------------------------------------------------------------------------
    // 401 – Unauthorized
    // -------------------------------------------------------------------------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                                                               HttpServletRequest request) {
        return respond(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 403 – Forbidden
    // -------------------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                             HttpServletRequest request) {
        return respond(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // 500 – Internal Server Error
    // -------------------------------------------------------------------------

    @ExceptionHandler(CreditScoringException.class)
    public ResponseEntity<ErrorResponse> handleCreditScoring(CreditScoringException ex,
                                                              HttpServletRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex,
                                                        HttpServletRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }
}
