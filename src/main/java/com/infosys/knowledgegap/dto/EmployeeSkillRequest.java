package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeSkillRequest {

    @NotNull
    private Long skillId;

    @NotNull
    private ProficiencyLevel proficiencyLevel;

    private Integer selfRating;
}
