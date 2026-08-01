package com.brandon.finance.auth.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.brandon.finance.auth.model.AuthenticatedUser;
import com.brandon.finance.auth.service.JwtService;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.UnauthorizedException;
import com.brandon.finance.shared.base.excepetion.record.ApiError;
import com.brandon.finance.user.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            jwtService.validateToken(token);

        } catch (UnauthorizedException ex) {

            writeUnauthorizedResponse(
                    request,
                    response,
                    ex.getErrorCode(),
                    ex.getMessage());

            return;
        }

        Long userId = jwtService.extractUserId(token);
        String email = jwtService.extractEmail(token);
        Role role = Role.valueOf(jwtService.extractRole(token));

        if (email != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role.name()));

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    email,
                    role);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode errorCode,
            String message) throws IOException {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                errorCode.name(),
                message,
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        objectMapper.writeValue(
                response.getOutputStream(),
                error);
    }
}
