package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.AssessmentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillAssessmentSubmissionRequest {

    // For SELF this is ignored server-side and forced to the caller's own id.
    @NotNull(message = "The person being assessed is required")
    private Long assessedUserId;

    @NotNull(message = "Skill is required")
    private Long skillId;

    @NotNull(message = "Assessment type is required")
    private AssessmentType type;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String comments;
}
