package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkExperienceRequest {

    @NotBlank
    private String companyOrProject;

    private String roleTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
