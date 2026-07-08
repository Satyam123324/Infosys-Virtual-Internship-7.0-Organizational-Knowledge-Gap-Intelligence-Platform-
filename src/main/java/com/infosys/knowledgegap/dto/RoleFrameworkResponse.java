package com.infosys.knowledgegap.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleFrameworkResponse {
    private Long id;
    private String roleTitle;
    private Long departmentId;
    private String departmentName;
    private String version;
    private List<CompetencyRequirementDto> requirements;
    private LocalDateTime createdAt;
}
