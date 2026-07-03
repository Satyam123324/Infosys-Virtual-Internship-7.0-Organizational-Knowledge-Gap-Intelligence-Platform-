package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.entity.PasswordResetToken;
import com.infosys.knowledgegap.entity.RefreshToken;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.AuthProvider;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.exception.DuplicateResourceException;
import com.infosys.knowledgegap.exception.InvalidPasswordException;
import com.infosys.knowledgegap.exception.InvalidTokenException;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.PasswordResetTokenRepository;
import com.infosys.knowledgegap.repository.RefreshTokenRepository;
import com.infosys.knowledgegap.repository.RoleRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.security.JwtService;
import com.infosys.knowledgegap.service.AuthService;
import com.infosys.knowledgegap.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.password-reset.token-expiration-minutes}")
    private long resetTokenExpirationMinutes;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        // Self-registration is restricted to EMPLOYEE role by default.
        // Elevated roles (HR Specialist, Department Head, L&D Admin, System Administrator)
        // must be assigned by a System Administrator via the Role Management module.
        Role assignedRole = roleRepository.findByName(RoleType.EMPLOYEE)
                .orElseThrow(() -> new ResourceNotFoundException("Default role EMPLOYEE not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .roles(Set.of(assignedRole))
                .build();

        user = userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        RefreshToken storedToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not recognized. Please login again"));

        if (storedToken.isRevoked() || !storedToken.getToken().equals(token)
                || storedToken.getExpiryDate().isBefore(Instant.now())
                || jwtService.isTokenExpired(token)) {
            throw new InvalidTokenException("Refresh token expired or invalid. Please login again");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(extractRoleNames(user))
                .build();
    }

    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(resetTokenExpirationMinutes * 60))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Password reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Invalidate any existing sessions by clearing refresh tokens
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ---------- helpers ----------

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken tokenEntity = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder().user(user).build());
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        tokenEntity.setRevoked(false);
        refreshTokenRepository.save(tokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(extractRoleNames(user))
                .build();
    }

    private Set<String> extractRoleNames(User user) {
        return user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
    }
}
