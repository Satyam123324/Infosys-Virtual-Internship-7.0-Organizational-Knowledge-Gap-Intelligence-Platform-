package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeRunRequest {

    @NotBlank
    private String language;

    @NotBlank
    private String code;

    private String stdin;
}
