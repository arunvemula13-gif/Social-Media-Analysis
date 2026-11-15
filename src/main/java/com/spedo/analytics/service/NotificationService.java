package com.spedo.analytics.service;

import com.spedo.analytics.dto.NotificationDto;
import com.spedo.analytics.entity.Notification;
import com.spedo.analytics.entity.NotificationType;
import com.spedo.analytics.entity.User;
import com.spedo.analytics.exception.ResourceNotFoundException;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EntityDtoMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, EntityDtoMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId, Boolean unreadOnly) {
        List<Notification> notifications;
        if (Boolean.TRUE.equals(unreadOnly)) {
            notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return notifications.stream().map(mapper::toNotificationDto).toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDto markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapper.toNotificationDto(saved);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public Notification createNotification(User user, String title, String message, NotificationType type, String link) {
        Notification notification = new Notification(user, title, message, type, link);
        return notificationRepository.save(notification);
    }
}
