package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.RoleFrameworkRequest;
import com.infosys.knowledgegap.dto.RoleFrameworkResponse;
import com.infosys.knowledgegap.service.CompetencyFrameworkService;
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
@RequestMapping("/api/v1/competency-frameworks")
@RequiredArgsConstructor
@Tag(name = "Competency Framework", description = "Module 3 — Role-based skill requirement definitions")
public class CompetencyFrameworkController {

    private final CompetencyFrameworkService frameworkService;

    @GetMapping
    @Operation(summary = "List all current role competency frameworks")
    public ResponseEntity<ApiResponse<List<RoleFrameworkResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Frameworks fetched", frameworkService.getAllFrameworks()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a framework by ID")
    public ResponseEntity<ApiResponse<RoleFrameworkResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Framework fetched", frameworkService.getFrameworkById(id)));
    }

    @GetMapping("/by-role/{roleTitle}")
    @Operation(summary = "Get the competency framework for a specific role title")
    public ResponseEntity<ApiResponse<RoleFrameworkResponse>> getByRoleTitle(@PathVariable String roleTitle) {
        return ResponseEntity.ok(ApiResponse.success("Framework fetched", frameworkService.getFrameworkByRoleTitle(roleTitle)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST','LEARNING_DEVELOPMENT_ADMIN')")
    @Operation(summary = "Create or update the competency framework for a role")
    public ResponseEntity<ApiResponse<RoleFrameworkResponse>> createOrUpdate(@Valid @RequestBody RoleFrameworkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Framework saved", frameworkService.createOrUpdateFramework(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','HR_SPECIALIST')")
    @Operation(summary = "Delete a competency framework")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        frameworkService.deleteFramework(id);
        return ResponseEntity.ok(ApiResponse.success("Framework deleted", null));
    }
}
