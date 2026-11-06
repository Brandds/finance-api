package com.brandon.finance.notification.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brandon.finance.authentication.service.AuthenticatedUserService;
import com.brandon.finance.notification.dto.NotificationDTO;
import com.brandon.finance.notification.event.NotificationRequestedEvent;
import com.brandon.finance.notification.mapper.NotificationMapper;
import com.brandon.finance.notification.repository.NotificationRepository;
import com.brandon.finance.shared.base.enums.ErrorCode;
import com.brandon.finance.shared.base.excepetion.BusinessException;
import com.brandon.finance.shared.base.excepetion.ResourceNotFoundException;
import com.brandon.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<NotificationDTO> getByUser(Pageable pageable) {
        return notificationRepository.findByUserId(authenticatedUserService.getUserId(), pageable)
            .map(notificationMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByUserIdAndReadFalse(authenticatedUserService.getUserId());
    }

    @Transactional
    public NotificationDTO markAsRead(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "ID da notificação inválido");
        }
        var notification = notificationRepository.findByIdAndUserId(id, authenticatedUserService.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        if (!notification.isRead()) {
            notification.markAsRead(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return notificationMapper.toDTO(notification);
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead(authenticatedUserService.getUserId(), LocalDateTime.now());
    }

    /** Criação reservada a eventos internos; o destinatário não vem de uma requisição HTTP. */
    @Transactional
    public void create(NotificationRequestedEvent event) {
        if (event == null || event.userId() == null || event.userId() <= 0 || event.type() == null
                || event.title() == null || event.title().isBlank() || event.title().length() > 150
                || event.message() == null || event.message().isBlank() || event.message().length() > 1000) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Dados da notificação inválidos");
        }
        var user = userRepository.findById(event.userId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário da notificação não encontrado"));
        notificationRepository.save(notificationMapper.toEntity(event, user));
    }
}
