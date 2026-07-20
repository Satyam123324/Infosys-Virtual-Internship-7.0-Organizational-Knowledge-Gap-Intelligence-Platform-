package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.learning.CourseRequest;
import com.infosys.knowledgegap.dto.learning.CourseResponse;
import com.infosys.knowledgegap.service.CourseService;
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
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Learning Course Management")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Get all courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        return ResponseEntity.ok(
                ApiResponse.success("Courses fetched", courseService.getAllCourses())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Course fetched", courseService.getCourseById(id))
        );
    }

    @GetMapping("/skill/{skillId}")
    @Operation(summary = "Get courses by skill")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesBySkill(@PathVariable Long skillId) {
        return ResponseEntity.ok(
                ApiResponse.success("Courses fetched", courseService.getCoursesBySkill(skillId))
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN','HR_SPECIALIST')")
    @Operation(summary = "Create course")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Course created",
                        courseService.createCourse(request)
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN','HR_SPECIALIST')")
    @Operation(summary = "Update course")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Course updated",
                        courseService.updateCourse(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN')")
    @Operation(summary = "Delete course")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {

        courseService.deleteCourse(id);

        return ResponseEntity.ok(
                ApiResponse.success("Course deleted", null)
        );
    }
}