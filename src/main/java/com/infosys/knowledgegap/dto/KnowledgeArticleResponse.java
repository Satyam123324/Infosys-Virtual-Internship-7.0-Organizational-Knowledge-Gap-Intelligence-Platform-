package com.infosys.knowledgegap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeArticleResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Long skillId;
    private String skillName;
    private Long authorId;
    private String authorName;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Lets the frontend show edit/delete controls only to the person who can actually use them.
    private boolean canEdit;
}
