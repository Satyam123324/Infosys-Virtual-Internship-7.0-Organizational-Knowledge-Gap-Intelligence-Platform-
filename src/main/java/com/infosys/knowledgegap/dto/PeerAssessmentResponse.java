package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PeerAssessmentResponse {
    private Long id;
    private Long raterUserId;
    private String raterName;
    private Long targetUserId;
    private String targetName;
    private Long skillId;
    private String skillName;
    private ProficiencyLevel ratedLevel;
    private String comment;
    private LocalDateTime createdAt;
}
