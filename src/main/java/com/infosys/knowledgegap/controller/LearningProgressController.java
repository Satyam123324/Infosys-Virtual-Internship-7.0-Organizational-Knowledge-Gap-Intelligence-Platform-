package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.learning.LearningProgressRequest;
import com.infosys.knowledgegap.dto.learning.LearningProgressResponse;
import com.infosys.knowledgegap.service.LearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-progress")
@RequiredArgsConstructor
@Tag(name = "Learning Progress", description = "Employee Learning Progress APIs")
public class LearningProgressController {

    private final LearningProgressService learningProgressService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN','MANAGER')")
    @Operation(summary = "Enroll employee into a course")
    public ResponseEntity<ApiResponse<LearningProgressResponse>> enroll(
            @Valid @RequestBody LearningProgressRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Enrollment successful",
                        learningProgressService.enrollCourse(request)
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update learning progress")
    public ResponseEntity<ApiResponse<LearningProgressResponse>> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer percentage) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Progress updated",
                        learningProgressService.updateProgress(id, percentage)
                )
        );
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get employee learning progress")
    public ResponseEntity<ApiResponse<List<LearningProgressResponse>>> getEmployeeProgress(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Learning progress fetched",
                        learningProgressService.getEmployeeProgress(employeeId)
                )
        );
    }

    @GetMapping
    @Operation(summary = "Get all learning progress")
    public ResponseEntity<ApiResponse<List<LearningProgressResponse>>> getAllProgress() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Learning progress fetched",
                        learningProgressService.getAllProgress()
                )
        );
    }
}