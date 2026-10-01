package com.freelancehub.service;

import com.freelancehub.model.Notification;
import com.freelancehub.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public ArrayList<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public void markAsRead(Long id) {
        notificationRepository.markAsRead(id);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}
