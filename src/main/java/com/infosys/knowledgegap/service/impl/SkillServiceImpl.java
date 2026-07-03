package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.SkillCategoryDto;
import com.infosys.knowledgegap.dto.SkillDto;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.entity.SkillCategory;
import com.infosys.knowledgegap.exception.DuplicateResourceException;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.SkillCategoryRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillCategoryRepository categoryRepository;

    @Override
    public SkillCategoryDto createCategory(SkillCategoryDto dto) {
        SkillCategory category = SkillCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        category = categoryRepository.save(category);
        return SkillCategoryDto.builder()
                .id(category.getId()).name(category.getName()).description(category.getDescription()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> SkillCategoryDto.builder().id(c.getId()).name(c.getName()).description(c.getDescription()).build())
                .collect(Collectors.toList());
    }

    @Override
    public SkillDto createSkill(SkillDto dto) {
        if (skillRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Skill already exists: " + dto.getName());
        }
        SkillCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill category not found"));

        Skill skill = Skill.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(category)
                .active(true)
                .build();
        return toDto(skillRepository.save(skill));
    }

    @Override
    public SkillDto updateSkill(Long id, SkillDto dto) {
        Skill skill = findById(id);
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        if (dto.getCategoryId() != null) {
            SkillCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill category not found"));
            skill.setCategory(category);
        }
        return toDto(skillRepository.save(skill));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkills() {
        return skillRepository.findByActiveTrue().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDto> getSkillsByCategory(Long categoryId) {
        return skillRepository.findByCategoryId(categoryId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SkillDto getSkillById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public void deactivateSkill(Long id) {
        Skill skill = findById(id);
        skill.setActive(false);
        skillRepository.save(skill);
    }

    private Skill findById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + id));
    }

    private SkillDto toDto(Skill s) {
        return SkillDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .categoryId(s.getCategory() != null ? s.getCategory().getId() : null)
                .categoryName(s.getCategory() != null ? s.getCategory().getName() : null)
                .active(s.isActive())
                .build();
    }
}
