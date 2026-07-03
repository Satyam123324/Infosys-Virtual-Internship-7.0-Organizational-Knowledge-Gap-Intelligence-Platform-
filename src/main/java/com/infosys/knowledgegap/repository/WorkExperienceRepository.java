package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByEmployeeProfileId(Long employeeProfileId);
}
