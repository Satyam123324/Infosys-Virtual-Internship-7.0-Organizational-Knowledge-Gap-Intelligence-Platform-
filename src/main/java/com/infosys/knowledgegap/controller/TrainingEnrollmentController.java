package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.TeamMemberLearningProgressResponse;
import com.infosys.knowledgegap.dto.TrainingEnrollmentRequest;
import com.infosys.knowledgegap.dto.TrainingEnrollmentResponse;
import com.infosys.knowledgegap.dto.TrainingProgressUpdateRequest;
import com.infosys.knowledgegap.service.TrainingEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MINIMAL STUB module (see TrainingEnrollment entity Javadoc) — enrollment +
 * progress tracking only, no course catalog/content. Exists to drive the
 * Employee Dashboard progress bar and the Notification module's deadline
 * reminders + completion milestones.
 */
@RestController
@RequestMapping("/api/v1/training/enrollments")
@RequiredArgsConstructor
@Tag(name = "Training Enrollments", description = "Enroll in courses and track progress toward deadlines")
public class TrainingEnrollmentController {

    private final TrainingEnrollmentService trainingEnrollmentService;

    @PostMapping
    @Operation(summary = "Enroll myself in a training course")
    public ResponseEntity<ApiResponse<TrainingEnrollmentResponse>> enroll(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TrainingEnrollmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Enrolled",
                trainingEnrollmentService.enroll(userDetails.getUsername(), request)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get all of my training enrollments")
    public ResponseEntity<ApiResponse<List<TrainingEnrollmentResponse>>> getMyEnrollments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Enrollments fetched",
                trainingEnrollmentService.getMyEnrollments(userDetails.getUsername())));
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "Update progress on a course (100% automatically marks it complete and awards a milestone)")
    public ResponseEntity<ApiResponse<TrainingEnrollmentResponse>> updateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TrainingProgressUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Progress updated",
                trainingEnrollmentService.updateProgress(userDetails.getUsername(), id, request.getProgressPercent())));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel/withdraw from a training enrollment")
    public ResponseEntity<ApiResponse<Void>> cancelEnrollment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        trainingEnrollmentService.cancelEnrollment(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment cancelled", null));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Org-wide per-person training progress — frontend filters by department for team-scoped roles, matching the existing employee-profile/all convention")
    public ResponseEntity<ApiResponse<List<TeamMemberLearningProgressResponse>>> getTeamProgress() {
        return ResponseEntity.ok(ApiResponse.success("Team progress fetched",
                trainingEnrollmentService.getTeamProgress()));
    }
}
