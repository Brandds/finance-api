package com.brandon.finance.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.shared.storage.StorageService;
import com.brandon.finance.shared.storage.StorageUnavailableException;
import com.brandon.finance.user.entity.User;
import com.brandon.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAvatarService {
    private final AuthenticatedUserService authenticatedUserService;
    private final UserRepository userRepository;
    private final AvatarImageValidator validator;
    private final StorageService storage;

    @Transactional
    public void upload(MultipartFile file) {
        Long userId = authenticatedUserService.getUserId();
        var image = validator.normalize(file);
        var user = lockUser(userId);
        String publicId = "finance-api/avatars/" + userId + "/" + UUID.randomUUID();

        reconcileAfterCompletion(user.getAvatarPublicId(), publicId);
        storage.upload(publicId, image.bytes(), image.contentType());
        user.setAvatarPublicId(publicId);
    }

    @Transactional
    public void delete() {
        var user = lockUser(authenticatedUserService.getUserId());
        if (user.getAvatarPublicId() != null) {
            reconcileAfterCompletion(user.getAvatarPublicId(), null);
            user.setAvatarPublicId(null);
        }
    }

    private User lockUser(Long userId) {
        return userRepository.findByIdForAvatarUpdate(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado"));
    }

    private void reconcileAfterCompletion(String previousId, String newId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteObsolete(previousId);
            }

            @Override
            public void afterCompletion(int status) {
                switch (status) {
                    case STATUS_COMMITTED -> { }
                    case STATUS_ROLLED_BACK -> {
                        try {
                            deleteObsolete(newId);
                        } catch (StorageUnavailableException ignored) {
                            // Preserve the original failure. Cleanup failures are logged below.
                        }
                    }
                    default -> log.warn("Resultado da transação de avatar incerto: anterior={}, nova={}",
                        previousId, newId);
                }
            }
        });
    }

    private void deleteObsolete(String publicId) {
        if (publicId == null) {
            return;
        }
        try {
            storage.delete(publicId);
        } catch (StorageUnavailableException ex) {
            log.warn("Falha na limpeza de avatar. public_id para reconciliação: {}", publicId);
            throw ex;
        }
    }
}
