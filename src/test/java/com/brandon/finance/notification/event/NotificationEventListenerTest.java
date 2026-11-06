package com.brandon.finance.notification.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.brandon.finance.notification.enums.NotificationType;
import com.brandon.finance.notification.service.NotificationService;

class NotificationEventListenerTest {
    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void publishedBudgetAndSecurityEventsReachNotificationService(NotificationType type) {
        try (var context = new AnnotationConfigApplicationContext()) {
            var service = mock(NotificationService.class);
            context.registerBean(NotificationService.class, () -> service);
            context.register(NotificationEventListener.class);
            context.refresh();

            var event = new NotificationRequestedEvent(42L, type, "Alerta", "Mensagem");
            context.publishEvent(event);

            verify(service).create(event);
        }
    }
}
