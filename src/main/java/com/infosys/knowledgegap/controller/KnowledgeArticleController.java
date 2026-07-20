package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.KnowledgeArticleRequest;
import com.infosys.knowledgegap.dto.KnowledgeArticleResponse;
import com.infosys.knowledgegap.service.KnowledgeArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Employee-contributed knowledge base — any authenticated user can publish an
 * article, optionally tagged to a skill. Any authenticated user can read;
 * only the author or an admin/L&D role can edit or delete.
 */
@RestController
@RequestMapping("/api/v1/knowledge-articles")
@RequiredArgsConstructor
@Tag(name = "Knowledge Article Library", description = "Employee-contributed knowledge base, optionally tagged to skills")
public class KnowledgeArticleController {

    private final KnowledgeArticleService knowledgeArticleService;

    @PostMapping
    @Operation(summary = "Publish a new article")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody KnowledgeArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Article published",
                knowledgeArticleService.create(userDetails.getUsername(), request)));
    }

    @GetMapping
    @Operation(summary = "Browse/search the library — optional ?search= text query or ?skillId= filter")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long skillId) {
        return ResponseEntity.ok(ApiResponse.success("Articles fetched",
                knowledgeArticleService.getAll(userDetails.getUsername(), search, skillId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single article (increments its view count)")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Article fetched",
                knowledgeArticleService.getById(userDetails.getUsername(), id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an article (author or admin/L&D only)")
    public ResponseEntity<ApiResponse<KnowledgeArticleResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeArticleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Article updated",
                knowledgeArticleService.update(userDetails.getUsername(), id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an article (author or admin/L&D only)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        knowledgeArticleService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Article deleted", null));
    }
}
