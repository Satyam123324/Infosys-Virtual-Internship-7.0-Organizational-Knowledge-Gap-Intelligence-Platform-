package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PeerAssessmentRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private Long skillId;

    @NotNull
    private ProficiencyLevel ratedLevel;

    private String comment;
}
