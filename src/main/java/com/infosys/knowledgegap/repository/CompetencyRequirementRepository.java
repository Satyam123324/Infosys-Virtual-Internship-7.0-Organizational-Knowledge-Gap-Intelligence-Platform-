package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.CompetencyRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompetencyRequirementRepository extends JpaRepository<CompetencyRequirement, Long> {
    List<CompetencyRequirement> findByFrameworkId(Long frameworkId);
}
