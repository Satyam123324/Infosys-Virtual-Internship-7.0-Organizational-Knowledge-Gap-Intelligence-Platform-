package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssessmentResultResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double scorePercent;
    private ProficiencyLevel computedLevel;
    private LocalDateTime takenAt;
}
