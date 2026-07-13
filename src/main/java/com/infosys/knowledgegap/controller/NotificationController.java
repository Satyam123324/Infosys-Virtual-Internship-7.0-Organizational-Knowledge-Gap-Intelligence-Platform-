package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.ApiResponse;
import com.infosys.knowledgegap.dto.NotificationSummary;
import com.infosys.knowledgegap.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Module 9 — Gap alerts and certification expiry reminders")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @Operation(summary = "Get my notifications (auto-generates fresh alerts from live gap/certification data)")
    public ResponseEntity<ApiResponse<NotificationSummary>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched",
                notificationService.getMyNotifications(userDetails.getUsername())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dismiss/delete a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.deleteNotification(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification dismissed", null));
    }
}
