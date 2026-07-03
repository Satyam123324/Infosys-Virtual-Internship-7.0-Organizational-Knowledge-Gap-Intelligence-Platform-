package com.infosys.knowledgegap.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkExperienceResponse {
    private Long id;
    private String companyOrProject;
    private String roleTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
