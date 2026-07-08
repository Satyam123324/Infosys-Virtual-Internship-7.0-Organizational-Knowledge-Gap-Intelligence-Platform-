package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long> {
    List<CodingProblem> findByLanguage(String language);
}
