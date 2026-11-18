package com.brandon.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import com.brandon.finance.auth.model.AuthenticatedUser;
import com.brandon.finance.shared.storage.StorageService;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.enums.Role;
import com.brandon.finance.user.repository.UserRepository;
import com.brandon.finance.user.service.UserAvatarService;

@SpringBootTest
class FinanceApiApplicationTests {
    @Autowired UserRepository users;
    @Autowired UserAvatarService avatars;
    @Autowired TransactionTemplate transaction;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean StorageService storage;

    @Test
    void contextLoads() {
    }

    @Test
    void persistsAvatarPublicIdInPostgresAndCompensatesOnRollback() throws IOException {
        String email = "avatar-" + UUID.randomUUID() + "@example.test";
        String cpf = Long.toString(ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L));
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", bytes);
        var photo = new MockMultipartFile("image", "photo.png", "image/png", bytes.toByteArray());

        String publicId = transaction.execute(status -> {
            var user = users.saveAndFlush(new User("Avatar Test", email, "test-placeholder", Role.USER, cpf));
            var principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
            SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
            try {
                avatars.upload(photo);
                users.flush();
                String savedId = jdbc.queryForObject("select avatar_public_id from users where id = ?", String.class,
                    user.getId());
                assertThat(savedId).startsWith("finance-api/avatars/" + user.getId() + "/")
                    .matches("finance-api/avatars/" + user.getId() + "/[a-f0-9-]{36}");
                return savedId;
            } finally {
                status.setRollbackOnly();
                SecurityContextHolder.clearContext();
            }
        });

        assertThat(users.findByEmail(email)).isEmpty();
        verify(storage).upload(eq(publicId), any(byte[].class), eq("image/png"));
        verify(storage).delete(publicId);
    }
}
