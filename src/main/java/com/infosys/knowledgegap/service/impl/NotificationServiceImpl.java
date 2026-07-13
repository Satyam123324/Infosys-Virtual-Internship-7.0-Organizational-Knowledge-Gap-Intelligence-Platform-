package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.EmployeeGapReport;
import com.infosys.knowledgegap.dto.NotificationResponse;
import com.infosys.knowledgegap.dto.NotificationSummary;
import com.infosys.knowledgegap.dto.SkillGapDetail;
import com.infosys.knowledgegap.entity.Certification;
import com.infosys.knowledgegap.entity.EmployeeProfile;
import com.infosys.knowledgegap.entity.Notification;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.NotificationType;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.CertificationRepository;
import com.infosys.knowledgegap.repository.EmployeeProfileRepository;
import com.infosys.knowledgegap.repository.NotificationRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.GapAnalysisService;
import com.infosys.knowledgegap.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final CertificationRepository certificationRepository;
    private final GapAnalysisService gapAnalysisService;

    @Override
    public NotificationSummary getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        syncGapNotifications(user);
        syncCertificationNotifications(user);

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
            if (notificationRepository.findByUserIdAndDedupeKey(user.getId(), dedupeKey).isPresent()) continue;

            NotificationType type = "CRITICAL".equals(gap.getSeverity())
                    ? NotificationType.CRITICAL_GAP : NotificationType.MODERATE_GAP;

            String title = ("CRITICAL".equals(gap.getSeverity()) ? "Critical" : "Moderate") + " skill gap: " + gap.getSkillName();
            String message = String.format("Your %s proficiency is %s but your role requires %s.",
                    gap.getSkillName(),
                    gap.getCurrentLevel() != null ? gap.getCurrentLevel().name() : "not assessed",
                    gap.getRequiredLevel().name());

            notificationRepository.save(Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .dedupeKey(dedupeKey)
                    .read(false)
                    .build());
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
            if (notificationRepository.findByUserIdAndDedupeKey(user.getId(), dedupeKey).isPresent()) continue;

            String title = expired ? "Certification expired: " + cert.getName() : "Certification expiring soon: " + cert.getName();
            String message = expired
                    ? String.format("Your %s certification expired on %s. Consider renewing it.", cert.getName(), cert.getExpiryDate())
                    : String.format("Your %s certification expires on %s.", cert.getName(), cert.getExpiryDate());

            notificationRepository.save(Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .dedupeKey(dedupeKey)
                    .read(false)
                    .build());
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
