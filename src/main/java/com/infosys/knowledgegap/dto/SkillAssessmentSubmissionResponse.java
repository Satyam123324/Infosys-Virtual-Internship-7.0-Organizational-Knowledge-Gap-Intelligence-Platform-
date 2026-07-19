package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillAssessmentSubmissionResponse {
    private Long id;
    private Long assessedUserId;
    private String assessedUserName;
    private Long assessorId;
    private String assessorName;
    private Long skillId;
    private String skillName;
    private AssessmentType type;
    private Integer rating;
    private String comments;
    private LocalDateTime submittedAt;
}
