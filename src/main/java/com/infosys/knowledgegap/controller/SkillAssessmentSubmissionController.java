package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.service.SkillAssessmentSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Self/peer/manager skill rating FORMS — distinct from the MCQ-based quiz in
 * AssessmentController (Module 8, SELF-only, auto-graded). This is a simple
 * 1-5 rating + comments, submittable about yourself (SELF), a colleague
 * (PEER), or — if you hold a manager/HR/admin role — a report (MANAGER).
 * There's no reporting-line model yet, so MANAGER access is role-gated
 * rather than tied to a specific person's actual manager.
 */
@RestController
@RequestMapping("/api/v1/skill-reviews")
@RequiredArgsConstructor
@Tag(name = "Skill Review Forms", description = "Self/peer/manager skill rating submissions")
public class SkillAssessmentSubmissionController {

    private final SkillAssessmentSubmissionService skillAssessmentSubmissionService;

    @PostMapping
    @Operation(summary = "Submit a self, peer, or manager skill rating")
    public ResponseEntity<ApiResponse<SkillAssessmentSubmissionResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SkillAssessmentSubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Assessment submitted",
                skillAssessmentSubmissionService.submit(userDetails.getUsername(), request)));
    }

    @GetMapping("/received")
    @Operation(summary = "Assessments received about me, across self/peer/manager")
    public ResponseEntity<ApiResponse<List<SkillAssessmentSubmissionResponse>>> getReceived(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Fetched",
                skillAssessmentSubmissionService.getReceivedByMe(userDetails.getUsername())));
    }

    @GetMapping("/submitted")
    @Operation(summary = "Assessments I've submitted about others")
    public ResponseEntity<ApiResponse<List<SkillAssessmentSubmissionResponse>>> getSubmitted(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Fetched",
                skillAssessmentSubmissionService.getSubmittedByMe(userDetails.getUsername())));
    }

    @GetMapping("/reviewable-users")
    @Operation(summary = "List of colleagues available to rate (for the peer/manager form's picker)")
    public ResponseEntity<ApiResponse<List<ReviewableUserResponse>>> getReviewableUsers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Fetched",
                skillAssessmentSubmissionService.getReviewableUsers(userDetails.getUsername())));
    }
}
