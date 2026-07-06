package com.infosys.knowledgegap.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssessmentQuestionDto {
    private Long id;
    private String questionText;
    private String codeSnippet;
    private List<String> options;
    // correctOptionIndex is intentionally excluded — never send the answer to the client
}
