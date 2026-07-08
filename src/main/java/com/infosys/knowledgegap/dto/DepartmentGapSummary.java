package com.infosys.knowledgegap.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DepartmentGapSummary {
    private String departmentName;
    private int employeeCount;
    private double avgReadinessPercent;
    private int totalGaps;
    private int criticalGaps;
}
