package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * MINIMAL STUB — no expert directory, no booking flow, no availability
 * matching. Exists only so the Notification module has real sessions to
 * remind people about. Replace/extend once the full Mentorship module
 * (directory + scheduling UI) is built.
 */
@Entity
@Table(name = "mentorship_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorshipSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @Column(nullable = false, length = 150)
    private String topic;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(length = 500)
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
