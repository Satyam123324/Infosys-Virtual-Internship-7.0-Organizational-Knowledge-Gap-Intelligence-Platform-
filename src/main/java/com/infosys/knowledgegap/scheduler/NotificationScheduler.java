package com.infosys.knowledgegap.scheduler;

import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Drives the "reminder" notification types (training deadlines, mentorship
 * sessions, certification expiry, skill gaps) proactively instead of only
 * generating them when a user happens to open the notification bell.
 * Milestones and new-recommendation alerts are event-driven instead — see
 * NotificationService.notifyMilestoneAchieved / notifyNewRecommendation,
 * which should be called directly from the Training/Recommendation services
 * the moment those events happen.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Runs daily at 07:00 server time.
    @Scheduled(cron = "0 0 7 * * *")
    public void runDailySweep() {
        List<User> users = userRepository.findAll();
        log.info("Running daily notification sweep for {} users", users.size());

        int failures = 0;
        for (User user : users) {
            try {
                notificationService.generateNotificationsForUser(user);
            } catch (Exception ex) {
                failures++;
                log.error("Notification sweep failed for user {}: {}", user.getEmail(), ex.getMessage());
            }
        }
        log.info("Daily notification sweep complete ({} failures)", failures);
    }
}
