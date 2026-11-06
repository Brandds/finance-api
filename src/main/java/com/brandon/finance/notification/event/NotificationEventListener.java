package com.brandon.finance.notification.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.brandon.finance.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void onNotificationRequested(NotificationRequestedEvent event) {
        notificationService.create(event);
    }
}
