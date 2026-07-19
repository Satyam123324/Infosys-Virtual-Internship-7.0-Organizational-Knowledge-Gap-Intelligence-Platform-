package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findAllByOrderByCreatedAtDesc();

    List<KnowledgeArticle> findBySkillIdOrderByCreatedAtDesc(Long skillId);

    List<KnowledgeArticle> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Query("SELECT a FROM KnowledgeArticle a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.summary) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "ORDER BY a.createdAt DESC")
    List<KnowledgeArticle> search(@Param("q") String query);
}
