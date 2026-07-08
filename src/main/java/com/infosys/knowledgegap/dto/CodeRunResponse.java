package com.infosys.knowledgegap.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeRunResponse {
    private String stdout;
    private String stderr;
    private Integer exitCode;
}
