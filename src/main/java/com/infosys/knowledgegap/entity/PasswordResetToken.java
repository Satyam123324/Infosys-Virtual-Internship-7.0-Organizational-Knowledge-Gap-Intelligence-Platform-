package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String token; // 6-digit numeric OTP

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OtpPurpose purpose = OtpPurpose.FORGOT_PASSWORD;

    @Builder.Default
    private boolean used = false;
}
