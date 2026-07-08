package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Long> {
    List<CodingSubmission> findByEmployeeProfileIdOrderBySubmittedAtDesc(Long employeeProfileId);
}
