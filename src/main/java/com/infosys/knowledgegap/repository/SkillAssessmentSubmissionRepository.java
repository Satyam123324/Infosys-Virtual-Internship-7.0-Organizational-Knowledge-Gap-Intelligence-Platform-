package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.SkillAssessmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillAssessmentSubmissionRepository extends JpaRepository<SkillAssessmentSubmission, Long> {
    List<SkillAssessmentSubmission> findByAssessedUserIdOrderBySubmittedAtDesc(Long assessedUserId);
    List<SkillAssessmentSubmission> findByAssessorIdOrderBySubmittedAtDesc(Long assessorId);
}
