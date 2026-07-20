package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, Long> {
    List<TrainingEnrollment> findByUserIdAndCompletedFalse(Long userId);
    List<TrainingEnrollment> findByUserIdOrderByDeadlineAsc(Long userId);
}
