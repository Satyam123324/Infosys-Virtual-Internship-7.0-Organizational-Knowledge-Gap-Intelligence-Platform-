package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.DepartmentGapSummary;
import com.infosys.knowledgegap.dto.EmployeeGapReport;
import com.infosys.knowledgegap.service.GapAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gap-analysis")
@RequiredArgsConstructor
@Tag(name = "Gap Analysis Engine", description = "Module 4 — Detects skill gaps vs role requirements with AI-style recommendations")
public class GapAnalysisController {

    private final GapAnalysisService gapAnalysisService;

    @GetMapping("/me")
    @Operation(summary = "Get my personal gap report against my current role's requirements")
    public ResponseEntity<ApiResponse<EmployeeGapReport>> getMyReport(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Gap report generated",
                gapAnalysisService.getMyGapReport(userDetails.getUsername())));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Get a specific employee's gap report")
    public ResponseEntity<ApiResponse<EmployeeGapReport>> getReportForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Gap report generated",
                gapAnalysisService.getGapReportForUser(userId)));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','DEPARTMENT_HEAD','TEAM_LEAD_MANAGER')")
    @Operation(summary = "Get gap reports for every employee in a department")
    public ResponseEntity<ApiResponse<List<EmployeeGapReport>>> getReportsForDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success("Gap reports generated",
                gapAnalysisService.getGapReportsForDepartment(departmentId)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST')")
    @Operation(summary = "Get gap reports for every employee in the organization")
    public ResponseEntity<ApiResponse<List<EmployeeGapReport>>> getAllReports() {
        return ResponseEntity.ok(ApiResponse.success("Gap reports generated",
                gapAnalysisService.getAllGapReports()));
    }

    @GetMapping("/department-summary")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST')")
    @Operation(summary = "Get aggregated gap statistics per department, for the admin dashboard")
    public ResponseEntity<ApiResponse<List<DepartmentGapSummary>>> getDepartmentSummaries() {
        return ResponseEntity.ok(ApiResponse.success("Department gap summaries generated",
                gapAnalysisService.getDepartmentGapSummaries()));
    }
}
