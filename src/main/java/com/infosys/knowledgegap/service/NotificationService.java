package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.NotificationSummary;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.MilestoneType;

public interface NotificationService {

    NotificationSummary getMyNotifications(String email);

    void markAsRead(String email, Long notificationId);

    void markAllAsRead(String email);

    void deleteNotification(String email, Long notificationId);

    /**
     * Runs every notification check (gaps, certifications, training deadlines,
     * mentorship reminders) for one user and persists + emails anything new.
     * Called on-demand from getMyNotifications() and in bulk by the nightly
     * NotificationScheduler so reminders go out even if the user never opens
     * the notification bell.
     */
    void generateNotificationsForUser(User user);

    /**
     * Event-driven hook — call this the moment a new AI/skill recommendation
     * is generated for a user (e.g. from RecommendationService) to alert them.
     */
    void notifyNewRecommendation(User user, String skillName, String recommendationSummary);

    /**
     * Event-driven hook — call this the moment a milestone/achievement is
     * earned (e.g. course completed, skill mastered) to notify + email the user.
     */
    void notifyMilestoneAchieved(User user, MilestoneType type, String title, String description, String badgeIcon, Long milestoneId);
}
