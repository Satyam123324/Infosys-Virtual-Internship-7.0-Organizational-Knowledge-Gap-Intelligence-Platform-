package com.infosys.knowledgegap;

import com.infosys.knowledgegap.dto.RegisterRequest;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.exception.DuplicateResourceException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.security.JwtService;
import com.infosys.knowledgegap.service.EmailService;
import com.infosys.knowledgegap.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;

    @InjectMocks AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetTokenExpirationMinutes", 30L);
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("Test User", "test@example.com", "Test@1234", "Eng", "Dev", null);
        Role role = Role.builder().id(1L).name(RoleType.EMPLOYEE).build();

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByName(RoleType.EMPLOYEE)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            return User.builder().id(1L).fullName(u.getFullName()).email(u.getEmail())
                    .password(u.getPassword()).roles(u.getRoles()).enabled(true).build();
        });
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");
        when(refreshTokenRepository.findByUser(any())).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = authService.register(req);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.getRoles().contains("EMPLOYEE"));
        verify(emailService).sendWelcomeEmail(any(), any());
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest req = new RegisterRequest("X", "dup@test.com", "Test@1234", null, null, null);
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> authService.register(req));
    }
}
