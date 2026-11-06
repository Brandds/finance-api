package com.brandon.finance.notification.mapper;

import org.springframework.stereotype.Component;

import com.brandon.finance.notification.dto.NotificationDTO;
import com.brandon.finance.notification.entity.Notification;
import com.brandon.finance.notification.event.NotificationRequestedEvent;
import com.brandon.finance.user.entity.User;

@Component
public class NotificationMapper {
    public NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(notification.getId(), notification.getType(), notification.getTitle(),
            notification.getMessage(), notification.isRead(), notification.getCreatedAt(), notification.getReadAt());
    }

    public Notification toEntity(NotificationRequestedEvent event, User user) {
        var notification = new Notification();
        notification.setUser(user);
        notification.setType(event.type());
        notification.setTitle(event.title());
        notification.setMessage(event.message());
        return notification;
    }
}
