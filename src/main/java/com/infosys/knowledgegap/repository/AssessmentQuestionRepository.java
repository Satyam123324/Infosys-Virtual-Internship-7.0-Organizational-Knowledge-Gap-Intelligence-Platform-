package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    List<AssessmentQuestion> findBySkillId(Long skillId);
}
