package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.MilestoneType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an earned achievement/badge. `badgeIcon` is a free-text hint
 * (e.g. a lucide-react icon name like "trophy" or "flame") the frontend can
 * map to an icon for the milestone/achievement badges UI.
 */
@Entity
@Table(name = "learning_milestones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 300)
    private String description;

    @Column(length = 50)
    private String badgeIcon;

    @Column(updatable = false)
    private LocalDateTime achievedAt;

    @PrePersist
    protected void onCreate() { achievedAt = LocalDateTime.now(); }
}
