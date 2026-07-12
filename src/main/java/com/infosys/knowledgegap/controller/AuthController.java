package com.infosys.knowledgegap.controller;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT + OAuth2 login, registration, OTP-based password management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new employee account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Get a new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    // ---------- Forgot Password (OTP) ----------

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a 6-digit OTP via email to reset a forgotten password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "If an account with that email exists, a verification code has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the 6-digit OTP sent to your email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    // ---------- Change Password (OTP, while logged in) ----------

    @PostMapping("/change-password/request-otp")
    @Operation(summary = "Send a 6-digit OTP to my own email to authorize a password change")
    public ResponseEntity<ApiResponse<Void>> requestChangePasswordOtp(
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.requestChangePasswordOtp(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Verification code sent to your email", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password (requires current password + emailed OTP)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
