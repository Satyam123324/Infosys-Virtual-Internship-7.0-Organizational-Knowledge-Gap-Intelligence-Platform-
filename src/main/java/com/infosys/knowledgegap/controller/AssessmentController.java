package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
@Tag(name = "Assessment & Survey", description = "Module 8 — Skill self-assessment quiz with auto-graded proficiency")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/questions/{skillId}")
    @Operation(summary = "Get quiz questions for a skill (answers withheld)")
    public ResponseEntity<ApiResponse<List<AssessmentQuestionDto>>> getQuestions(@PathVariable Long skillId) {
        return ResponseEntity.ok(ApiResponse.success("Questions fetched",
                assessmentService.getQuestionsForSkill(skillId)));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit quiz answers — auto-grades and updates the employee's skill proficiency")
    public ResponseEntity<ApiResponse<AssessmentResultResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AssessmentSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Assessment submitted",
                assessmentService.submitAssessment(userDetails.getUsername(), request)));
    }

    @GetMapping("/my-results")
    @Operation(summary = "Get my past assessment results")
    public ResponseEntity<ApiResponse<List<AssessmentResultResponse>>> getMyResults(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Results fetched",
                assessmentService.getMyResults(userDetails.getUsername())));
    }
}
