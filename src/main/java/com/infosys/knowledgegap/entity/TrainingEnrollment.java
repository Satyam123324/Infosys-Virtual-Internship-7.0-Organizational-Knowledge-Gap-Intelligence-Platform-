package com.infosys.knowledgegap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MINIMAL STUB — this is not the full Training module (no course catalog, no
 * content, no scoring). It exists only to give the Notification module real
 * data to react to (deadline reminders + progress-driven milestones) while
 * the full Training/Learning module is built out separately. Extend or
 * replace this once that module lands — dedupe keys in NotificationService
 * reference "training:{id}" so keep the id stable if you migrate the table.
 */
@Entity
@Table(name = "training_enrollments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrainingEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String courseName;

    @Column(length = 100)
    private String provider;

    @Column(nullable = false)
    private LocalDate deadline;

    @Builder.Default
    @Column(nullable = false)
    private int progressPercent = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean completed = false;

    @Column(updatable = false)
    private LocalDateTime enrolledAt;

    @PrePersist
    protected void onCreate() { enrolledAt = LocalDateTime.now(); }
}
