package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.RoleCompetencyFramework;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoleCompetencyFrameworkRepository extends JpaRepository<RoleCompetencyFramework, Long> {
    List<RoleCompetencyFramework> findByCurrentTrue();
    Optional<RoleCompetencyFramework> findByRoleTitleAndCurrentTrue(String roleTitle);
    List<RoleCompetencyFramework> findByDepartmentIdAndCurrentTrue(Long departmentId);
}
