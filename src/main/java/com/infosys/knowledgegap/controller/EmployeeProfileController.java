package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.service.EmployeeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-profile")
@RequiredArgsConstructor
@Tag(name = "Employee Profile & Skill Inventory", description = "Module 2 — self-service skill profile, certifications, work history")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    // ---------- Profile ----------

    @GetMapping("/me")
    @Operation(summary = "Get my employee profile (creates one on first access)")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                employeeProfileService.getOrCreateMyProfile(userDetails.getUsername())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my employee profile (department, role, joining date)")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EmployeeProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                employeeProfileService.updateMyProfile(userDetails.getUsername(), request)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Get an employee's profile by user ID (manager/HR view)")
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                employeeProfileService.getProfileByUserId(userId)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Get all employee profiles (org-wide skill inventory)")
    public ResponseEntity<ApiResponse<List<EmployeeProfileResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Profiles fetched", employeeProfileService.getAllProfiles()));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Get all employee profiles in a department")
    public ResponseEntity<ApiResponse<List<EmployeeProfileResponse>>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success("Profiles fetched",
                employeeProfileService.getProfilesByDepartment(departmentId)));
    }

    // ---------- Skills ----------

    @PostMapping("/me/skills")
    @Operation(summary = "Add or update a skill self-assessment")
    public ResponseEntity<ApiResponse<EmployeeSkillResponse>> addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EmployeeSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Skill saved",
                employeeProfileService.addOrUpdateSkill(userDetails.getUsername(), request)));
    }

    @GetMapping("/me/skills")
    @Operation(summary = "Get my skill inventory")
    public ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>> getMySkills(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Skills fetched",
                employeeProfileService.getMySkills(userDetails.getUsername())));
    }

    @DeleteMapping("/me/skills/{skillId}")
    @Operation(summary = "Remove a skill from my inventory")
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long skillId) {
        employeeProfileService.removeSkill(userDetails.getUsername(), skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }

    // ---------- Certifications ----------

    @PostMapping("/me/certifications")
    @Operation(summary = "Add a certification")
    public ResponseEntity<ApiResponse<CertificationResponse>> addCertification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CertificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Certification added",
                employeeProfileService.addCertification(userDetails.getUsername(), request)));
    }

    @GetMapping("/me/certifications")
    @Operation(summary = "Get my certifications")
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> getMyCertifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Certifications fetched",
                employeeProfileService.getMyCertifications(userDetails.getUsername())));
    }

    @DeleteMapping("/me/certifications/{certificationId}")
    @Operation(summary = "Delete a certification")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long certificationId) {
        employeeProfileService.deleteCertification(userDetails.getUsername(), certificationId);
        return ResponseEntity.ok(ApiResponse.success("Certification deleted", null));
    }

    // ---------- Work Experience ----------

    @PostMapping("/me/experience")
    @Operation(summary = "Add a work experience entry")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> addExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Experience added",
                employeeProfileService.addWorkExperience(userDetails.getUsername(), request)));
    }

    @GetMapping("/me/experience")
    @Operation(summary = "Get my work experience history")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getMyExperience(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Experience fetched",
                employeeProfileService.getMyWorkExperience(userDetails.getUsername())));
    }

    @DeleteMapping("/me/experience/{workExperienceId}")
    @Operation(summary = "Delete a work experience entry")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long workExperienceId) {
        employeeProfileService.deleteWorkExperience(userDetails.getUsername(), workExperienceId);
        return ResponseEntity.ok(ApiResponse.success("Experience deleted", null));
    }
}
