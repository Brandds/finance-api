package com.brandon.finance.auth.google.service;

import com.brandon.finance.auth.google.dto.GoogleTokenResponse;
import com.brandon.finance.auth.google.dto.GoogleUserInfo;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private final WebClient webClient = WebClient.builder().build();

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    // 🔥 Método principal
    public User processGoogleLogin(String code) {
        GoogleTokenResponse token = exchangeCodeForToken(code);
        GoogleUserInfo userInfo = parseIdToken(token.idToken());

        return findOrCreateUser(userInfo);
    }

    // 🔄 1. Trocar code por token
    private GoogleTokenResponse exchangeCodeForToken(String code) {
        return webClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "code=" + code +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&redirect_uri=" + redirectUri +
                        "&grant_type=authorization_code"
                )
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();
    }

    // 🔐 2. Decodificar e validar id_token
    private GoogleUserInfo parseIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            validateToken(claims);

            return new GoogleUserInfo(
                    (String) claims.get("sub"),
                    (String) claims.get("email"),
                    (String) claims.get("name"),
                    (String) claims.get("picture"),
                    (Boolean) claims.get("email_verified")
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar id_token", e);
        }
    }

    // 🔍 3. Validações básicas de segurança
    private void validateToken(Map<String, Object> claims) {
        if (!clientId.equals(claims.get("aud"))) {
            throw new RuntimeException("Token inválido: client_id diferente");
        }

        if (!Boolean.TRUE.equals(claims.get("email_verified"))) {
            throw new RuntimeException("Email não verificado no Google");
        }
    }

    // 👤 4. Criar ou buscar usuário
    private User findOrCreateUser(GoogleUserInfo info) {

        // 🔎 Primeiro tenta por googleId
        return userRepository.findByGoogleId(info.sub())
                .orElseGet(() -> {

                    // 🔎 Depois tenta por email
                    return userRepository.findByEmail(info.email())
                            .map(existingUser -> {
                                existingUser.setGoogleId(info.sub());
                                existingUser.updateFromGoogleInfo(info.name(), info.picture());
                                return userRepository.save(existingUser);
                            })
                            .orElseGet(() -> {
                                // 🆕 Criar novo usuário
                                User newUser = new User();
                                newUser.setEmail(info.email());
                                newUser.setName(info.name());
                                newUser.setGoogleId(info.sub());
                                newUser.setGooglePictureUrl(info.picture());

                                return userRepository.save(newUser);
                            });
                });
    }
}
