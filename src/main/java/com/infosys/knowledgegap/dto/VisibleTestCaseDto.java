package com.infosys.knowledgegap.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VisibleTestCaseDto {
    private String stdin;
    private String expectedOutput;
}
