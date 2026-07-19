package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    /** Used to de-duplicate — e.g. "gap:Java" or "cert:12" so we don't spam repeat alerts. */
    @Column(nullable = false, length = 100)
    private String dedupeKey;

    /** Optional id of the source record (training enrollment, session, milestone, etc.) for deep-linking in the UI. */
    private Long referenceId;

    @Builder.Default
    private boolean read = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
