package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.CodingTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodingTestCaseRepository extends JpaRepository<CodingTestCase, Long> {
    List<CodingTestCase> findByProblemId(Long problemId);
}
