package com.infosys.knowledgegap.entity;

import com.infosys.knowledgegap.enums.AssessmentType;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee_skills", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_profile_id", "skill_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_profile_id", nullable = false)
    private EmployeeProfile employeeProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel proficiencyLevel;

    @Enumerated(EnumType.STRING)
    private AssessmentType assessmentType;

    private LocalDate lastAssessedDate;

    private Integer selfRating;
    private Integer peerRating;
    private Integer managerRating;
}
