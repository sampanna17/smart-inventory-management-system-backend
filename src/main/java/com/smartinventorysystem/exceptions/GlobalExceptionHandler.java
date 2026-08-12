package com.smartinventorysystem.exceptions;

import com.smartinventorysystem.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(EmailAlreadyExistedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExistedException(EmailAlreadyExistedException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(UnauthorizedException ex) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(
            DisabledException ex) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Account disabled",
                List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception caught in GlobalExceptionHandler: ", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                ex.getMessage() != null ? List.of(ex.getMessage()) : null
        );
    }

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageUploadException(Exception ex) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                List.of(ex.getMessage())
        );
    }

    @ExceptionHandler(ImageNotBelongToProductException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageNotBelongToProductException(Exception ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        );
    }
    @ExceptionHandler(InvalidSaleStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSaleStatus(InvalidSaleStatusException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status,
            String message,
            List<String> errors) {

        return ResponseEntity.status(status)
                .body(ApiResponse.<Void>builder()
                        .status(status.value())
                        .success(false)
                        .message(message)
                        .errors(errors)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
