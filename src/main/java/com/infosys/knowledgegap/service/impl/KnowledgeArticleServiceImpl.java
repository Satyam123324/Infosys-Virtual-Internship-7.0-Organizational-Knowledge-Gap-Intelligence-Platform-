package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.KnowledgeArticleRequest;
import com.infosys.knowledgegap.dto.KnowledgeArticleResponse;
import com.infosys.knowledgegap.entity.KnowledgeArticle;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.KnowledgeArticleRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.KnowledgeArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    @Override
    public KnowledgeArticleResponse create(String email, KnowledgeArticleRequest request) {
        User author = getUser(email);
        Skill skill = resolveSkill(request.getSkillId());

        KnowledgeArticle article = knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .content(request.getContent())
                .skill(skill)
                .author(author)
                .build());

        return toResponse(article, author);
    }

    @Override
    public List<KnowledgeArticleResponse> getAll(String email, String search, Long skillId) {
        User viewer = getUser(email);

        List<KnowledgeArticle> articles;
        if (skillId != null) {
            articles = knowledgeArticleRepository.findBySkillIdOrderByCreatedAtDesc(skillId);
        } else if (search != null && !search.isBlank()) {
            articles = knowledgeArticleRepository.search(search.trim());
        } else {
            articles = knowledgeArticleRepository.findAllByOrderByCreatedAtDesc();
        }

        return articles.stream().map(a -> toResponse(a, viewer)).collect(Collectors.toList());
    }

    @Override
    public KnowledgeArticleResponse getById(String email, Long articleId) {
        User viewer = getUser(email);
        KnowledgeArticle article = getArticle(articleId);

        article.setViewCount(article.getViewCount() + 1);
        knowledgeArticleRepository.save(article);

        return toResponse(article, viewer);
    }

    @Override
    public KnowledgeArticleResponse update(String email, Long articleId, KnowledgeArticleRequest request) {
        User user = getUser(email);
        KnowledgeArticle article = getArticle(articleId);
        assertCanEdit(user, article);

        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setSkill(resolveSkill(request.getSkillId()));

        knowledgeArticleRepository.save(article);
        return toResponse(article, user);
    }

    @Override
    public void delete(String email, Long articleId) {
        User user = getUser(email);
        KnowledgeArticle article = getArticle(articleId);
        assertCanEdit(user, article);
        knowledgeArticleRepository.delete(article);
    }

    // ---------- helpers ----------

    private void assertCanEdit(User user, KnowledgeArticle article) {
        if (!canEdit(user, article)) {
            throw new IllegalArgumentException("Only the author or an admin can modify this article");
        }
    }

    private boolean canEdit(User user, KnowledgeArticle article) {
        if (article.getAuthor().getId().equals(user.getId())) return true;
        return user.getRoles().stream().anyMatch(r -> r.getName() == RoleType.SYSTEM_ADMINISTRATOR
                || r.getName() == RoleType.LEARNING_DEVELOPMENT_ADMIN);
    }

    private Skill resolveSkill(Long skillId) {
        if (skillId == null) return null;
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
    }

    private KnowledgeArticle getArticle(Long id) {
        return knowledgeArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private KnowledgeArticleResponse toResponse(KnowledgeArticle a, User viewer) {
        return KnowledgeArticleResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .content(a.getContent())
                .skillId(a.getSkill() != null ? a.getSkill().getId() : null)
                .skillName(a.getSkill() != null ? a.getSkill().getName() : null)
                .authorId(a.getAuthor().getId())
                .authorName(a.getAuthor().getFullName())
                .viewCount(a.getViewCount())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .canEdit(canEdit(viewer, a))
                .build();
    }
}
