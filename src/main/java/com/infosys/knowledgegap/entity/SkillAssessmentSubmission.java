package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A single self/peer/manager rating of someone's skill proficiency, via a
 * simple form (1-5 scale + optional comments) — distinct from the MCQ quiz
 * (see AssessmentQuestion/AssessmentResult, which is SELF-only and
 * auto-graded). Multiple submissions can accumulate over time for the same
 * assessedUser+skill+type pair; the latest of each type also gets mirrored
 * onto EmployeeSkill.selfRating/peerRating/managerRating for quick display.
 */
@Entity
@Table(name = "skill_assessment_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillAssessmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_user_id", nullable = false)
    private User assessedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessor_id", nullable = false)
    private User assessor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType type;

    /** 1 (UNAWARE) through 5 (EXPERT) — mirrors ProficiencyLevel ordinal + 1. */
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comments;

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() { submittedAt = LocalDateTime.now(); }
}
