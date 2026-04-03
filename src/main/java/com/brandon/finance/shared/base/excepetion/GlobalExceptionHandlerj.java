package com.brandon.finance.shared.base.excepetion;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.record.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandlerj {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                new BusinessException(ErrorCode.INTERNAL_ERROR, "Erro interno"), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação");

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, BusinessException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(error);
    }
}
