package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssessmentSubmitRequest {

    @NotNull
    private Long skillId;

    @NotEmpty
    private List<AssessmentAnswerRequest> answers;
}
