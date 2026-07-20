package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    List<LearningProgress> findByEmployeeId(Long employeeId);

}