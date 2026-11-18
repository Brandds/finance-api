package com.brandon.finance.shared.base.excepetion;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.record.ApiError;
import com.brandon.finance.shared.storage.StorageUnavailableException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ApiError> handleInvalidMultipart(HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(),
            "Upload multipart inválido ou parte obrigatória ausente", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadSize(HttpServletRequest request) {
        return buildError(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCode.VALIDATION_ERROR.name(),
            "Upload excede o tamanho permitido", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleUnsupportedMedia(HttpServletRequest request) {
        return buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.VALIDATION_ERROR.name(),
            "Tipo de conteúdo não aceito", request);
    }

    @ExceptionHandler(StorageUnavailableException.class)
    public ResponseEntity<ApiError> handleStorageUnavailable(StorageUnavailableException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.INTERNAL_ERROR.name(),
            ex.getMessage(), request);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(),
            "Corpo inválido: verifique os tipos dos campos e a data no formato yyyy-MM-dd", request);
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_ERROR.name(),
            "Parâmetro obrigatório ausente: " + ex.getParameterName(),
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
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        String message = "Dados já cadastrados";

        String cause = ex.getMostSpecificCause().getMessage();

        if (cause != null) {
            if (cause.contains("users_cpf_key")) {
                message = "CPF já cadastrado";
            } else if (cause.contains("users_email_key")) {
                message = "E-mail já cadastrado";
            }
        }

        return buildError(
                HttpStatus.CONFLICT,
                ErrorCode.CONFLICT.name(),
                message,
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
