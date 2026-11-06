package com.brandon.finance.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.email.service.impl.EmailServiceImpl;
import com.brandon.finance.email.service.impl.EmailVerificationTokenServiceImpl;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.enums.Role;
import com.brandon.finance.user.mapper.UserMapper;
import com.brandon.finance.user.repository.UserRepository;
import com.brandon.finance.user.request.CreateUserRequest;
import com.brandon.finance.user.response.UserEditResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository repository;
    @Spy UserMapper mapper = new UserMapper();
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailServiceImpl emailService;
    @Mock EmailVerificationTokenServiceImpl emailVerificationTokenService;
    @Mock AuthenticatedUserService authenticatedUserService;
    @InjectMocks UserService service;

    @Test
    void registersLocalUserDisabledAndSendsVerificationForSavedUser() {
        var request = new CreateUserRequest("Owner", "owner@example.test", "password", "529.982.247-25");
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(repository.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 42L);
            return user;
        });

        var response = service.create(request);

        var saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().isEnabled()).isFalse();
        assertThat(saved.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
        assertThat(response.id()).isEqualTo(42L);
        var token = ArgumentCaptor.forClass(String.class);
        verify(emailVerificationTokenService).create(token.capture(), org.mockito.ArgumentMatchers.same(saved.getValue()));
        verify(emailService).sendVerificationEmail(saved.getValue(), token.getValue());
    }

    @Test
    void listsUserResponses() {
        var user = user();
        when(repository.findAll()).thenReturn(List.of(user));

        assertThat(service.getAllUsers()).containsExactly(mapper.toResponse(user));
    }

    @Test
    void updatesOnlyOwnedUserWithoutChangingCredentialsOrRole() {
        var user = user();
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findById(42L)).thenReturn(Optional.of(user));

        var response = service.updateUser(42L, new UserEditResponse("Updated", "updated@example.test"));

        assertThat(response.name()).isEqualTo("Updated");
        assertThat(response.email()).isEqualTo("updated@example.test");
        assertThat(user.getPassword()).isEqualTo("stored-hash");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        verify(repository).save(user);
    }

    @Test
    void rejectsAnotherUsersIdBeforeReadingOrWritingUserData() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);

        assertThatThrownBy(() -> service.updateUser(43L, new UserEditResponse("Other", "other@example.test")))
            .isInstanceOf(AuthorizationDeniedException.class);
        assertThatThrownBy(() -> service.getUserById(43L)).isInstanceOf(AuthorizationDeniedException.class);
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void reportsMissingOwnedUser() {
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(42L, new UserEditResponse("Updated", "updated@example.test")))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void currentUserRoutesUseAuthenticatedUserService() {
        var user = user();
        when(authenticatedUserService.getUserId()).thenReturn(42L);
        when(repository.findById(42L)).thenReturn(Optional.of(user));

        assertThat(service.getCurrentUser().id()).isEqualTo(42L);
        assertThat(service.updateUserLogged(new UserEditResponse("Updated", "updated@example.test")).name())
            .isEqualTo("Updated");
    }

    private User user() {
        var user = new User(42L);
        user.setName("Owner");
        user.setEmail("owner@example.test");
        user.setPassword("stored-hash");
        user.setRole(Role.USER);
        user.setCpf("52998224725");
        user.setEnabled(true);
        return user;
    }
}
