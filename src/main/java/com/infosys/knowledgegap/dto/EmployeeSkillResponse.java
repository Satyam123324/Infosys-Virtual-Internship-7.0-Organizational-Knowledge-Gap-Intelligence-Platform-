package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.AssessmentType;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeSkillResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private String categoryName;
    private ProficiencyLevel proficiencyLevel;
    private AssessmentType assessmentType;
    private LocalDate lastAssessedDate;
    private Integer selfRating;
    private Integer peerRating;
    private Integer managerRating;
}
