package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.KnowledgeArticleRequest;
import com.infosys.knowledgegap.dto.KnowledgeArticleResponse;

import java.util.List;

public interface KnowledgeArticleService {

    KnowledgeArticleResponse create(String email, KnowledgeArticleRequest request);

    /** search and skillId are both optional; skillId takes priority if both are given. */
    List<KnowledgeArticleResponse> getAll(String email, String search, Long skillId);

    KnowledgeArticleResponse getById(String email, Long articleId);

    KnowledgeArticleResponse update(String email, Long articleId, KnowledgeArticleRequest request);

    void delete(String email, Long articleId);
}
