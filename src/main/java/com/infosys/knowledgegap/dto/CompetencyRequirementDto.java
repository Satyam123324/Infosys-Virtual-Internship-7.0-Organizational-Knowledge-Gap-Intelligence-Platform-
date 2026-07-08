package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CompetencyRequirementDto {
    private Long id;

    @NotNull
    private Long skillId;

    private String skillName;

    @NotNull
    private ProficiencyLevel requiredLevel;

    private boolean mandatory;
}
