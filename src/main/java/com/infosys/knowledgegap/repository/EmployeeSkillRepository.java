package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeProfileId(Long employeeProfileId);
    Optional<EmployeeSkill> findByEmployeeProfileIdAndSkillId(Long employeeProfileId, Long skillId);
    List<EmployeeSkill> findBySkillId(Long skillId);
    void deleteByEmployeeProfileIdAndSkillId(Long employeeProfileId, Long skillId);
}
