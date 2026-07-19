package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.LearningMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningMilestoneRepository extends JpaRepository<LearningMilestone, Long> {
    List<LearningMilestone> findByUserIdOrderByAchievedAtDesc(Long userId);
}
