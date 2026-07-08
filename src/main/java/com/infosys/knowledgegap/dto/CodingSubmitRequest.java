package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodingSubmitRequest {

    @NotNull
    private Long problemId;

    @NotBlank
    private String code;
}
