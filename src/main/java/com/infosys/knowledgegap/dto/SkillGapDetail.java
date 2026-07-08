package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillGapDetail {
    private Long skillId;
    private String skillName;
    private ProficiencyLevel requiredLevel;
    private ProficiencyLevel currentLevel; // null if employee has no rating at all
    private int gapSize; // requiredLevel - currentLevel, in levels (0 = no gap)
    private boolean mandatory;
    private String severity; // NONE, MINOR, MODERATE, CRITICAL

    // Populated only when gapSize > 0
    private String recommendationText;
    private List<ResourceLink> suggestedResources;
}
