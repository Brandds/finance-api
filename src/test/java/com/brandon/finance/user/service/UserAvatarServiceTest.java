package com.brandon.finance.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.shared.storage.StorageService;
import com.brandon.finance.shared.storage.StorageUnavailableException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {
    @Mock AuthenticatedUserService authenticatedUser;
    @Mock UserRepository users;
    @Mock AvatarImageValidator validator;
    @Mock StorageService storage;
    @InjectMocks UserAvatarService service;

    private final User user = new User(42L);
    private final MockMultipartFile file = new MockMultipartFile("image", new byte[]{1});

    @BeforeEach
    void startTransaction() {
        TransactionSynchronizationManager.initSynchronization();
        when(authenticatedUser.getUserId()).thenReturn(42L);
        when(users.findByIdForAvatarUpdate(42L)).thenReturn(Optional.of(user));
    }

    @AfterEach
    void endTransaction() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void uploadsUsingAuthenticatedIdAndDeletesPreviousObjectOnlyAfterCommit() {
        user.setAvatarPublicId("previous");
        when(validator.normalize(file)).thenReturn(new AvatarImageValidator.Image(new byte[]{2}, "image/png"));

        service.upload(file);

        assertThat(user.getAvatarPublicId()).matches("finance-api/avatars/42/[a-f0-9-]{36}");
        verify(storage).upload(eq(user.getAvatarPublicId()), eq(new byte[]{2}), eq("image/png"));
        verify(storage, never()).delete(any());
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCommit());
        verify(storage).delete("previous");
    }

    @Test
    void compensatesFailedPutWithoutReplacingPreviousAvatar() {
        user.setAvatarPublicId("previous");
        when(validator.normalize(file)).thenReturn(new AvatarImageValidator.Image(new byte[]{2}, "image/png"));
        doThrow(StorageUnavailableException.class).when(storage).upload(any(), any(), any());

        assertThatThrownBy(() -> service.upload(file)).isInstanceOf(StorageUnavailableException.class);
        assertThat(user.getAvatarPublicId()).isEqualTo("previous");
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(STATUS_ROLLED_BACK));

        var key = ArgumentCaptor.forClass(String.class);
        verify(storage).upload(key.capture(), any(), any());
        verify(storage).delete(key.getValue());
        verify(storage, never()).delete("previous");
    }

    @Test
    void removalClearsOnlyTheAvatarAndReportsCleanupFailure() {
        user.setAvatarPublicId("previous");
        user.updateFromGoogleInfo("Owner", "https://example.test/google.png");
        doThrow(StorageUnavailableException.class).when(storage).delete("previous");

        service.delete();

        assertThat(user.getAvatarPublicId()).isNull();
        assertThat(user.getGooglePictureUrl()).isEqualTo("https://example.test/google.png");
        assertThat(user.getName()).isEqualTo("Owner");
        assertThatThrownBy(() -> TransactionSynchronizationManager.getSynchronizations()
            .forEach(sync -> sync.afterCommit())).isInstanceOf(StorageUnavailableException.class);
    }

    @Test
    void removingAbsentAvatarIsIdempotent() {
        service.delete();

        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
        verifyNoInteractions(storage);
    }
}
