package com.brandon.finance.shared.base.excepetion;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.record.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

        // logar erro real
        log.error("Erro interno", ex);

        return buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_ERROR.name(),
            "Erro interno",
            request
        );
    }

   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return buildError(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_ERROR.name(),
            message,
            request
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundHandler(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            ErrorCode.RESOURCE_NOT_FOUND.name(),
            "Endpoint não encontrado",
            request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.UNAUTHORIZED, ex, request);
    }


    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.FORBIDDEN,
            ErrorCode.FORBIDDEN.name(),
            "Acesso negado",
            request
        );
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

    private ResponseEntity<ApiError> buildError(
        HttpStatus status,
        String errorCode,
        String message,
        HttpServletRequest request
    ) {
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            errorCode,
            message,
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }
}
