package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.PeerAssessmentRequest;
import com.infosys.knowledgegap.dto.PeerAssessmentResponse;
import com.infosys.knowledgegap.service.PeerAssessmentService;
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

@RestController
@RequestMapping("/api/v1/peer-assessments")
@RequiredArgsConstructor
@Tag(name = "Peer Assessment", description = "Module 1/8 — one employee rates a colleague's skill proficiency")
public class PeerAssessmentController {

    private final PeerAssessmentService peerAssessmentService;

    @PostMapping
    @Operation(summary = "Submit a peer assessment for a colleague's skill")
    public ResponseEntity<ApiResponse<PeerAssessmentResponse>> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PeerAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Peer assessment submitted",
                peerAssessmentService.submitPeerAssessment(userDetails.getUsername(), request)));
    }

    @GetMapping("/received")
    @Operation(summary = "Get peer assessments I have received from colleagues")
    public ResponseEntity<ApiResponse<List<PeerAssessmentResponse>>> getReceived(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Received assessments fetched",
                peerAssessmentService.getAssessmentsReceivedByMe(userDetails.getUsername())));
    }

    @GetMapping("/given")
    @Operation(summary = "Get peer assessments I have given to colleagues")
    public ResponseEntity<ApiResponse<List<PeerAssessmentResponse>>> getGiven(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Given assessments fetched",
                peerAssessmentService.getAssessmentsGivenByMe(userDetails.getUsername())));
    }
}
