package com.infosys.knowledgegap.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodingProblemDto {
    private Long id;
    private String title;
    private String description;
    private String language;
    private String difficulty;
    private String starterCode;
    private List<VisibleTestCaseDto> visibleTestCases; // hidden ones are never sent to client
}
