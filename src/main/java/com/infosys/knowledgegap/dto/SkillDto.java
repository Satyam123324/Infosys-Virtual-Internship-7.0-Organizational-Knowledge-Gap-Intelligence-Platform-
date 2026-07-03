package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillDto {
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long categoryId;

    private String categoryName;
    private boolean active;
}
