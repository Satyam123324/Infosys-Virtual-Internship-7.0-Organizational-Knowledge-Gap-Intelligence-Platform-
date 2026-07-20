package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.NotificationSummary;

public interface NotificationService {
    NotificationSummary getMyNotifications(String email);
    void markAsRead(String email, Long notificationId);
    void markAllAsRead(String email);
    void deleteNotification(String email, Long notificationId);
}
