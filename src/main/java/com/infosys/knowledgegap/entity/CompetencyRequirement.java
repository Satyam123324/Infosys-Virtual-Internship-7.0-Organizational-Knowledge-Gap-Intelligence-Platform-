package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "competency_requirements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"framework_id", "skill_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompetencyRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "framework_id", nullable = false)
    private RoleCompetencyFramework framework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel requiredLevel;

    @Builder.Default
    private boolean mandatory = true;
}
