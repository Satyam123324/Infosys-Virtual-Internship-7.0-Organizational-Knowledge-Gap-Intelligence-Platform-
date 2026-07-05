package com.infosys.knowledgegap.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssessmentAnswerRequest {
    private Long questionId;
    private Integer selectedOptionIndex;
}
