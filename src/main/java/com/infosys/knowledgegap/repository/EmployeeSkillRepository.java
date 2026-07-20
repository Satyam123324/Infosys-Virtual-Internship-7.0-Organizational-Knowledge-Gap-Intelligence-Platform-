package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.EmployeeSkill;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeProfileId(Long employeeProfileId);
    Optional<EmployeeSkill> findByEmployeeProfileIdAndSkillId(Long employeeProfileId, Long skillId);
    List<EmployeeSkill> findBySkillId(Long skillId);
    void deleteByEmployeeProfileIdAndSkillId(Long employeeProfileId, Long skillId);

    // Powers the "find someone who knows X" expert directory — only surfaces people
    // who are genuinely strong in a skill (ADVANCED/EXPERT), not every self-rating.
    List<EmployeeSkill> findBySkillIdAndProficiencyLevelIn(Long skillId, List<ProficiencyLevel> levels);
    List<EmployeeSkill> findByProficiencyLevelIn(List<ProficiencyLevel> levels);
}
