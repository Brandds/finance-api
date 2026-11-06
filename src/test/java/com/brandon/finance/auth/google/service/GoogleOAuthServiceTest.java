package com.brandon.finance.auth.google.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {
    @Mock UserRepository repository;
    private GoogleOAuthService service;

    @BeforeEach
    void setUp() throws Exception {
        var mapper = new ObjectMapper();
        service = new GoogleOAuthService(repository, mapper);
        ReflectionTestUtils.setField(service, "clientId", "test-client");
        String claims = Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(Map.of(
            "aud", "test-client", "sub", "google-id", "email", "owner@example.test",
            "name", "Owner", "picture", "https://example.test/picture", "email_verified", true)));
        String response = mapper.writeValueAsString(Map.of("id_token", "header." + claims + ".signature"));
        var client = WebClient.builder().exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).body(response).build())).build();
        ReflectionTestUtils.setField(service, "webClient", client);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsEnabledGoogleUserAfterProviderEmailVerification() {
        var user = service.processGoogleLogin("authorization-code");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getGoogleId()).isEqualTo("google-id");
        assertThat(user.getEmail()).isEqualTo("owner@example.test");
    }

    @Test
    void enablesPendingLocalAccountWhenLinkingVerifiedGoogleEmail() {
        var localUser = new User(42L);
        localUser.setEmail("owner@example.test");
        localUser.setPassword("stored-hash");
        assertThat(localUser.isEnabled()).isFalse();
        when(repository.findByEmail("owner@example.test")).thenReturn(Optional.of(localUser));

        var user = service.processGoogleLogin("authorization-code");

        assertThat(user).isSameAs(localUser);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getGoogleId()).isEqualTo("google-id");
        assertThat(user.getPassword()).isEqualTo("stored-hash");
    }
}
