package com.infosys.knowledgegap.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeGapReport {
    private Long userId;
    private String fullName;
    private String roleTitle;
    private String departmentName;
    private boolean frameworkFound;
    private int totalRequiredSkills;
    private int skillsMeetingRequirement;
    private int skillsWithGap;
    private double overallReadinessPercent;
    private List<SkillGapDetail> gaps;
}
