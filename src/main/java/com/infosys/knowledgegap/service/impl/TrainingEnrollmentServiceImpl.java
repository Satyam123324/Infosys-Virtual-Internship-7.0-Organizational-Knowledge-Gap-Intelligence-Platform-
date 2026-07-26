package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.LearningMilestoneResponse;
import com.infosys.knowledgegap.dto.TeamMemberLearningProgressResponse;
import com.infosys.knowledgegap.dto.TrainingEnrollmentRequest;
import com.infosys.knowledgegap.dto.TrainingEnrollmentResponse;
import com.infosys.knowledgegap.entity.LearningMilestone;
import com.infosys.knowledgegap.entity.TrainingEnrollment;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.MilestoneType;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.LearningMilestoneRepository;
import com.infosys.knowledgegap.repository.TrainingEnrollmentRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.NotificationService;
import com.infosys.knowledgegap.service.TrainingEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainingEnrollmentServiceImpl implements TrainingEnrollmentService {

    private final TrainingEnrollmentRepository trainingEnrollmentRepository;
    private final UserRepository userRepository;
    private final LearningMilestoneRepository learningMilestoneRepository;
    private final NotificationService notificationService;

    @Override
    public TrainingEnrollmentResponse enroll(String email, TrainingEnrollmentRequest request) {
        User user = getUser(email);

        TrainingEnrollment enrollment = trainingEnrollmentRepository.save(TrainingEnrollment.builder()
                .user(user)
                .courseName(request.getCourseName())
                .provider(request.getProvider())
                .deadline(request.getDeadline())
                .progressPercent(0)
                .completed(false)
                .build());

        return toResponse(enrollment);
    }

    @Override
    public List<TrainingEnrollmentResponse> getMyEnrollments(String email) {
        User user = getUser(email);
        return trainingEnrollmentRepository.findByUserIdOrderByDeadlineAsc(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningMilestoneResponse> getMyMilestones(String email) {
        User user = getUser(email);
        return learningMilestoneRepository.findByUserIdOrderByAchievedAtDesc(user.getId()).stream()
                .map(m -> LearningMilestoneResponse.builder()
                        .id(m.getId())
                        .type(m.getType())
                        .title(m.getTitle())
                        .description(m.getDescription())
                        .badgeIcon(m.getBadgeIcon())
                        .achievedAt(m.getAchievedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public TrainingEnrollmentResponse updateProgress(String email, Long enrollmentId, int progressPercent) {
        User user = getUser(email);
        TrainingEnrollment enrollment = getOwnedEnrollment(user, enrollmentId);

        boolean justCompleted = !enrollment.isCompleted() && progressPercent >= 100;

        enrollment.setProgressPercent(Math.min(progressPercent, 100));
        if (justCompleted) {
            enrollment.setCompleted(true);
        }
        trainingEnrollmentRepository.save(enrollment);

        if (justCompleted) {
            awardCompletionMilestone(user, enrollment);
        }

        return toResponse(enrollment);
    }

    @Override
    public void cancelEnrollment(String email, Long enrollmentId) {
        User user = getUser(email);
        TrainingEnrollment enrollment = getOwnedEnrollment(user, enrollmentId);
        trainingEnrollmentRepository.delete(enrollment);
    }

    @Override
    public List<TeamMemberLearningProgressResponse> getTeamProgress() {
        return userRepository.findAll().stream()
                .map(this::buildTeamMemberProgress)
                .collect(Collectors.toList());
    }

    private TeamMemberLearningProgressResponse buildTeamMemberProgress(User u) {
        List<TrainingEnrollment> enrollments = trainingEnrollmentRepository.findByUserIdOrderByDeadlineAsc(u.getId());
        LocalDate today = LocalDate.now();

        int total = enrollments.size();
        int completed = (int) enrollments.stream().filter(TrainingEnrollment::isCompleted).count();
        int overdue = (int) enrollments.stream()
                .filter(e -> !e.isCompleted() && e.getDeadline() != null && e.getDeadline().isBefore(today))
                .count();
        double avgProgress = enrollments.isEmpty() ? 0
                : enrollments.stream().mapToInt(TrainingEnrollment::getProgressPercent).average().orElse(0);
        int milestones = learningMilestoneRepository.findByUserIdOrderByAchievedAtDesc(u.getId()).size();

        return TeamMemberLearningProgressResponse.builder()
                .userId(u.getId())
                .fullName(u.getFullName())
                .department(u.getDepartment())
                .designation(u.getDesignation())
                .totalEnrollments(total)
                .completedCount(completed)
                .overdueCount(overdue)
                .avgProgressPercent(Math.round(avgProgress * 10) / 10.0)
                .milestonesEarned(milestones)
                .build();
    }

    // ---------- helpers ----------

    private void awardCompletionMilestone(User user, TrainingEnrollment enrollment) {
        String title = "Completed: " + enrollment.getCourseName();
        String description = String.format("You finished \"%s\"%s. Nice work!",
                enrollment.getCourseName(),
                enrollment.getProvider() != null && !enrollment.getProvider().isBlank()
                        ? " (" + enrollment.getProvider() + ")" : "");

        LearningMilestone milestone = learningMilestoneRepository.save(LearningMilestone.builder()
                .user(user)
                .type(MilestoneType.COURSE_COMPLETED)
                .title(title)
                .description(description)
                .badgeIcon("trophy")
                .build());

        notificationService.notifyMilestoneAchieved(
                user, MilestoneType.COURSE_COMPLETED, title, description, "trophy", milestone.getId());
    }

    private TrainingEnrollment getOwnedEnrollment(User user, Long enrollmentId) {
        TrainingEnrollment enrollment = trainingEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Training enrollment not found"));
        if (!enrollment.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Training enrollment not found");
        }
        return enrollment;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TrainingEnrollmentResponse toResponse(TrainingEnrollment e) {
        boolean overdue = !e.isCompleted() && e.getDeadline() != null && e.getDeadline().isBefore(LocalDate.now());
        return TrainingEnrollmentResponse.builder()
                .id(e.getId())
                .courseName(e.getCourseName())
                .provider(e.getProvider())
                .deadline(e.getDeadline())
                .progressPercent(e.getProgressPercent())
                .completed(e.isCompleted())
                .overdue(overdue)
                .enrolledAt(e.getEnrolledAt())
                .build();
    }
}
