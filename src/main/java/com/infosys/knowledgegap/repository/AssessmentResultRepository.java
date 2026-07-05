package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    List<AssessmentResult> findByEmployeeProfileIdOrderByTakenAtDesc(Long employeeProfileId);
}
