package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Double scorePercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel computedLevel;

    @Column(nullable = false)
    private LocalDateTime takenAt;
}
