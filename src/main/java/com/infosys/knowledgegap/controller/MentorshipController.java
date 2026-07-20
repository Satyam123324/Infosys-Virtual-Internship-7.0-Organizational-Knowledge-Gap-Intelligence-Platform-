package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.service.MentorshipService;
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
 * MINIMAL STUB module (see MentorshipSession entity Javadoc) — expert directory
 * is derived from existing EmployeeSkill proficiency data (ADVANCED/EXPERT only),
 * no dedicated mentor opt-in/availability system yet. Session booking has no
 * conflict detection against a mentor's calendar. Extend as the real Mentorship
 * module matures.
 */
@RestController
@RequestMapping("/api/v1/mentorship")
@RequiredArgsConstructor
@Tag(name = "Mentorship", description = "Expert directory search and mentorship session booking")
public class MentorshipController {

    private final MentorshipService mentorshipService;

    @GetMapping("/experts")
    @Operation(summary = "Find experts — pass ?skillName=Java to search, or omit to browse top experts across all skills")
    public ResponseEntity<ApiResponse<List<ExpertProfileResponse>>> findExperts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String skillName) {
        return ResponseEntity.ok(ApiResponse.success("Experts fetched",
                mentorshipService.findExperts(userDetails.getUsername(), skillName)));
    }

    @PostMapping("/sessions")
    @Operation(summary = "Book a mentorship session with a mentor")
    public ResponseEntity<ApiResponse<MentorshipSessionResponse>> bookSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MentorshipSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session booked",
                mentorshipService.bookSession(userDetails.getUsername(), request)));
    }

    @GetMapping("/sessions/me")
    @Operation(summary = "Get all my mentorship sessions (as mentor or mentee)")
    public ResponseEntity<ApiResponse<List<MentorshipSessionResponse>>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched",
                mentorshipService.getMySessions(userDetails.getUsername())));
    }

    @PatchMapping("/sessions/{id}/status")
    @Operation(summary = "Update a session's status (mentor or mentee can mark COMPLETED or CANCELLED)")
    public ResponseEntity<ApiResponse<MentorshipSessionResponse>> updateSessionStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SessionStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Session updated",
                mentorshipService.updateSessionStatus(userDetails.getUsername(), id, request.getStatus())));
    }
}
