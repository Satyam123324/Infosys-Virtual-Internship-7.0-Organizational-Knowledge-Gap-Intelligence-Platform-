package com.infosys.knowledgegap.repository;

import com.infosys.knowledgegap.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    boolean existsByName(String name);
    List<Skill> findByCategoryId(Long categoryId);
    List<Skill> findByActiveTrue();
}
