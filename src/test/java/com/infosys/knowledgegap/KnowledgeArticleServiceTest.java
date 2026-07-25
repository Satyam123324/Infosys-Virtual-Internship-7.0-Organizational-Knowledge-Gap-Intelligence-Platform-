package com.infosys.knowledgegap;

import com.infosys.knowledgegap.entity.KnowledgeArticle;
import com.infosys.knowledgegap.entity.Role;
import com.infosys.knowledgegap.entity.User;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.repository.KnowledgeArticleRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.repository.UserRepository;
import com.infosys.knowledgegap.service.impl.KnowledgeArticleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeArticleServiceTest {

    @Mock KnowledgeArticleRepository knowledgeArticleRepository;
    @Mock UserRepository userRepository;
    @Mock SkillRepository skillRepository;

    @InjectMocks KnowledgeArticleServiceImpl service;

    private User user(long id, String email, RoleType role) {
        return User.builder()
                .id(id).email(email)
                .roles(Set.of(Role.builder().id(1L).name(role).build()))
                .build();
    }

    @Test
    void delete_byNonAuthorNonAdmin_isRejected() {
        User caller = user(1L, "me@corp.com", RoleType.EMPLOYEE);
        User author = User.builder().id(2L).build();
        KnowledgeArticle article = KnowledgeArticle.builder().id(7L).author(author).title("Guide").build();

        when(userRepository.findByEmail("me@corp.com")).thenReturn(Optional.of(caller));
        when(knowledgeArticleRepository.findById(7L)).thenReturn(Optional.of(article));

        assertThrows(IllegalArgumentException.class, () -> service.delete("me@corp.com", 7L));
    }

    @Test
    void delete_byAuthor_succeeds() {
        User author = user(1L, "me@corp.com", RoleType.EMPLOYEE);
        KnowledgeArticle article = KnowledgeArticle.builder().id(7L).author(author).title("Guide").build();

        when(userRepository.findByEmail("me@corp.com")).thenReturn(Optional.of(author));
        when(knowledgeArticleRepository.findById(7L)).thenReturn(Optional.of(article));

        service.delete("me@corp.com", 7L);

        verify(knowledgeArticleRepository).delete(article);
    }
}
