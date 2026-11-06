package com.brandon.finance.notification.dto;

import java.time.LocalDateTime;

import com.brandon.finance.notification.enums.NotificationType;

public record NotificationDTO(
    Long id,
    NotificationType type,
    String title,
    String message,
    boolean read,
    LocalDateTime createdAt,
    LocalDateTime readAt
) {}
