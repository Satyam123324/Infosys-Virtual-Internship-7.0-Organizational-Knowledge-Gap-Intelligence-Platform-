package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.SkillCategoryDto;
import com.infosys.knowledgegap.dto.SkillDto;
import java.util.List;

public interface SkillService {
    SkillCategoryDto createCategory(SkillCategoryDto dto);
    List<SkillCategoryDto> getAllCategories();

    SkillDto createSkill(SkillDto dto);
    SkillDto updateSkill(Long id, SkillDto dto);
    List<SkillDto> getAllSkills();
    List<SkillDto> getSkillsByCategory(Long categoryId);
    SkillDto getSkillById(Long id);
    void deactivateSkill(Long id);
}
