package com.brandon.finance.shared.base.excepetion;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.record.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        ApiError  error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED.name(),
                "Não autenticado",
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}