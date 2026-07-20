package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnowledgeArticleRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    // Optional — leave null for a general article not tied to a specific skill.
    private Long skillId;
}
