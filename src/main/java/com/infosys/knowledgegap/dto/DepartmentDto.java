package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentDto {
    private Long id;

    @NotBlank
    private String name;

    private String description;
    private Long departmentHeadId;
    private String departmentHeadName;
}
