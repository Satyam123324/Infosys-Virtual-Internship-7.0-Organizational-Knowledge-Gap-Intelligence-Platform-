package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.EmployeeGapReport;
import com.infosys.knowledgegap.dto.NotificationResponse;
import com.infosys.knowledgegap.dto.NotificationSummary;
import com.infosys.knowledgegap.dto.SkillGapDetail;
import com.infosys.knowledgegap.entity.*;
import com.infosys.knowledgegap.enums.MilestoneType;
import com.infosys.knowledgegap.enums.NotificationType;
import com.infosys.knowledgegap.enums.SessionStatus;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.service.EmailService;
import com.infosys.knowledgegap.service.GapAnalysisService;
import com.infosys.knowledgegap.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final int TRAINING_DEADLINE_WARNING_DAYS = 3;
    private static final int MENTORSHIP_REMINDER_WINDOW_HOURS = 24;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final CertificationRepository certificationRepository;
    private final GapAnalysisService gapAnalysisService;
    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final EmailService emailService;

    @Override
    public NotificationSummary getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        generateNotificationsForUser(user);

        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long unread = notificationRepository.countByUserIdAndReadFalse(user.getId());

        return NotificationSummary.builder()
                .unreadCount(unread)
                .notifications(all.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    public void markAsRead(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        all.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(all);
    }

    @Override
    public void deleteNotification(String email, Long notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notificationRepository.delete(n);
    }

    @Override
    public void generateNotificationsForUser(User user) {
        // Each check is isolated — one module's data being incomplete/missing
        // shouldn't stop the others from generating alerts.
        safely(() -> syncGapNotifications(user));
        safely(() -> syncCertificationNotifications(user));
        safely(() -> syncTrainingDeadlineNotifications(user));
        safely(() -> syncMentorshipReminderNotifications(user));
    }

    @Override
    public void notifyNewRecommendation(User user, String skillName, String recommendationSummary) {
        String dedupeKey = "recommendation:" + skillName.toLowerCase();
        createIfAbsent(user, NotificationType.RECOMMENDATION_NEW,
                "New learning recommendation: " + skillName,
                recommendationSummary,
                dedupeKey, null);
    }

    @Override
    public void notifyMilestoneAchieved(User user, MilestoneType type, String title, String description, String badgeIcon, Long milestoneId) {
        String dedupeKey = "milestone:" + milestoneId;
        createIfAbsent(user, NotificationType.MILESTONE_ACHIEVED, title, description, dedupeKey, milestoneId);
    }

    // ---------- generation logic ----------

    private void syncGapNotifications(User user) {
        EmployeeGapReport report;
        try {
            report = gapAnalysisService.getMyGapReport(user.getEmail());
        } catch (Exception ex) {
            return; // no profile / no framework yet — nothing to notify about
        }
        if (!report.isFrameworkFound()) return;

        for (SkillGapDetail gap : report.getGaps()) {
            if (!"CRITICAL".equals(gap.getSeverity()) && !"MODERATE".equals(gap.getSeverity())) continue;

            String dedupeKey = "gap:" + gap.getSkillId() + ":" + gap.getSeverity();
            NotificationType type = "CRITICAL".equals(gap.getSeverity())
                    ? NotificationType.CRITICAL_GAP : NotificationType.MODERATE_GAP;

            String title = ("CRITICAL".equals(gap.getSeverity()) ? "Critical" : "Moderate") + " skill gap: " + gap.getSkillName();
            String message = String.format("Your %s proficiency is %s but your role requires %s.",
                    gap.getSkillName(),
                    gap.getCurrentLevel() != null ? gap.getCurrentLevel().name() : "not assessed",
                    gap.getRequiredLevel().name());

            createIfAbsent(user, type, title, message, dedupeKey, gap.getSkillId());
        }
    }

    private void syncCertificationNotifications(User user) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) return;

        List<Certification> certifications = certificationRepository.findByEmployeeProfileId(profile.getId());
        LocalDate today = LocalDate.now();
        LocalDate soonThreshold = today.plusDays(30);

        for (Certification cert : certifications) {
            if (cert.getExpiryDate() == null) continue;

            boolean expired = cert.getExpiryDate().isBefore(today);
            boolean expiringSoon = !expired && !cert.getExpiryDate().isAfter(soonThreshold);

            if (!expired && !expiringSoon) continue;

            NotificationType type = expired ? NotificationType.CERTIFICATION_EXPIRED : NotificationType.CERTIFICATION_EXPIRING;
            String dedupeKey = "cert:" + cert.getId() + ":" + type.name();

            String title = expired ? "Certification expired: " + cert.getName() : "Certification expiring soon: " + cert.getName();
            String message = expired
                    ? String.format("Your %s certification expired on %s. Consider renewing it.", cert.getName(), cert.getExpiryDate())
                    : String.format("Your %s certification expires on %s.", cert.getName(), cert.getExpiryDate());

            createIfAbsent(user, type, title, message, dedupeKey, cert.getId());
        }
    }

    private void syncTrainingDeadlineNotifications(User user) {
        List<TrainingEnrollment> enrollments = trainingEnrollmentRepository.findByUserIdAndCompletedFalse(user.getId());
        LocalDate today = LocalDate.now();
        LocalDate warningThreshold = today.plusDays(TRAINING_DEADLINE_WARNING_DAYS);

        for (TrainingEnrollment enrollment : enrollments) {
            if (enrollment.getDeadline() == null) continue;

            boolean overdue = enrollment.getDeadline().isBefore(today);
            boolean dueSoon = !overdue && !enrollment.getDeadline().isAfter(warningThreshold);

            if (!overdue && !dueSoon) continue;

            NotificationType type = overdue
                    ? NotificationType.TRAINING_DEADLINE_OVERDUE
                    : NotificationType.TRAINING_DEADLINE_APPROACHING;
            String dedupeKey = "training:" + enrollment.getId() + ":" + type.name();

            String title = overdue
                    ? "Training overdue: " + enrollment.getCourseName()
                    : "Training deadline approaching: " + enrollment.getCourseName();
            String message = overdue
                    ? String.format("\"%s\" was due on %s and is %d%% complete.",
                        enrollment.getCourseName(), enrollment.getDeadline(), enrollment.getProgressPercent())
                    : String.format("\"%s\" is due on %s. You're currently %d%% complete.",
                        enrollment.getCourseName(), enrollment.getDeadline(), enrollment.getProgressPercent());

            createIfAbsent(user, type, title, message, dedupeKey, enrollment.getId());
        }
    }

    private void syncMentorshipReminderNotifications(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(MENTORSHIP_REMINDER_WINDOW_HOURS);

        List<MentorshipSession> upcoming = mentorshipSessionRepository.findUpcomingForUser(
                user.getId(), SessionStatus.SCHEDULED, now, windowEnd);

        for (MentorshipSession session : upcoming) {
            String dedupeKey = "mentorship:" + session.getId();
            boolean isMentor = session.getMentor().getId().equals(user.getId());
            String counterpart = isMentor ? session.getMentee().getFullName() : session.getMentor().getFullName();

            String title = "Upcoming mentorship session: " + session.getTopic();
            String message = String.format("Your session on \"%s\" with %s is scheduled for %s.",
                    session.getTopic(), counterpart, session.getScheduledAt());

            createIfAbsent(user, NotificationType.MENTORSHIP_SESSION_REMINDER, title, message, dedupeKey, session.getId());
        }
    }

    /**
     * Core create path used by every notification source. Skips if this exact
     * dedupeKey already exists for the user (so reminders don't spam on every
     * scheduler run / every GET /me), persists the notification, and fires the
     * email channel. SMS/push are intentionally left as no-ops for now — wire
     * them in here once those providers are set up.
     */
    private void createIfAbsent(User user, NotificationType type, String title, String message, String dedupeKey, Long referenceId) {
        if (notificationRepository.findByUserIdAndDedupeKey(user.getId(), dedupeKey).isPresent()) return;

        Notification saved = notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .dedupeKey(dedupeKey)
                .referenceId(referenceId)
                .read(false)
                .build());

        emailService.sendNotificationEmail(user.getEmail(), title, message);
        log.debug("Created notification {} ({}) for user {}", saved.getId(), type, user.getEmail());
    }

    private void safely(Runnable task) {
        try {
            task.run();
        } catch (Exception ex) {
            log.error("Notification sync step failed: {}", ex.getMessage());
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
