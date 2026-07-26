package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.MilestoneType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningMilestoneResponse {
    private Long id;
    private MilestoneType type;
    private String title;
    private String description;
    private String badgeIcon;
    private LocalDateTime achievedAt;
}
