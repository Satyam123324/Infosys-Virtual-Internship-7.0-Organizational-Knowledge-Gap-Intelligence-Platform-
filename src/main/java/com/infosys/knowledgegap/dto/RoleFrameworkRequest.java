package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleFrameworkRequest {

    @NotBlank
    private String roleTitle;

    private Long departmentId;

    @NotEmpty
    private List<CompetencyRequirementDto> requirements;
}
