package com.brandon.finance.notification.event;

import com.brandon.finance.notification.enums.NotificationType;

/** Evento interno para solicitar alertas de orçamento ou segurança. */
public record NotificationRequestedEvent(Long userId, NotificationType type, String title, String message) {}
