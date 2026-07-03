package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.SkillCategoryDto;
import com.infosys.knowledgegap.dto.SkillDto;
import com.infosys.knowledgegap.service.SkillService;
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
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Skill catalog and category management")
public class SkillController {

    private final SkillService skillService;

    @GetMapping("/categories")
    @Operation(summary = "List all skill categories")
    public ResponseEntity<ApiResponse<List<SkillCategoryDto>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched", skillService.getAllCategories()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN','HR_SPECIALIST')")
    @Operation(summary = "Create a skill category")
    public ResponseEntity<ApiResponse<SkillCategoryDto>> createCategory(@Valid @RequestBody SkillCategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", skillService.createCategory(dto)));
    }

    @GetMapping
    @Operation(summary = "List all active skills")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getAllSkills() {
        return ResponseEntity.ok(ApiResponse.success("Skills fetched", skillService.getAllSkills()));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "List skills by category")
    public ResponseEntity<ApiResponse<List<SkillDto>>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success("Skills fetched", skillService.getSkillsByCategory(categoryId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID")
    public ResponseEntity<ApiResponse<SkillDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Skill fetched", skillService.getSkillById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN','HR_SPECIALIST')")
    @Operation(summary = "Create a skill in the catalog")
    public ResponseEntity<ApiResponse<SkillDto>> createSkill(@Valid @RequestBody SkillDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created", skillService.createSkill(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN','HR_SPECIALIST')")
    @Operation(summary = "Update a skill")
    public ResponseEntity<ApiResponse<SkillDto>> updateSkill(@PathVariable Long id, @Valid @RequestBody SkillDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Skill updated", skillService.updateSkill(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR','LEARNING_DEVELOPMENT_ADMIN')")
    @Operation(summary = "Deactivate a skill")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        skillService.deactivateSkill(id);
        return ResponseEntity.ok(ApiResponse.success("Skill deactivated", null));
    }
}
