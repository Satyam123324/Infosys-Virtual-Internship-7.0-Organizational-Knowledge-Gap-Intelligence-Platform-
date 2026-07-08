package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.CompetencyRequirementDto;
import com.infosys.knowledgegap.dto.RoleFrameworkRequest;
import com.infosys.knowledgegap.dto.RoleFrameworkResponse;
import com.infosys.knowledgegap.entity.CompetencyRequirement;
import com.infosys.knowledgegap.entity.Department;
import com.infosys.knowledgegap.entity.RoleCompetencyFramework;
import com.infosys.knowledgegap.entity.Skill;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.CompetencyRequirementRepository;
import com.infosys.knowledgegap.repository.DepartmentRepository;
import com.infosys.knowledgegap.repository.RoleCompetencyFrameworkRepository;
import com.infosys.knowledgegap.repository.SkillRepository;
import com.infosys.knowledgegap.service.CompetencyFrameworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompetencyFrameworkServiceImpl implements CompetencyFrameworkService {

    private final RoleCompetencyFrameworkRepository frameworkRepository;
    private final CompetencyRequirementRepository requirementRepository;
    private final SkillRepository skillRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public RoleFrameworkResponse createOrUpdateFramework(RoleFrameworkRequest request) {
        RoleCompetencyFramework framework = frameworkRepository
                .findByRoleTitleAndCurrentTrue(request.getRoleTitle())
                .orElse(RoleCompetencyFramework.builder()
                        .roleTitle(request.getRoleTitle())
                        .version("v1")
                        .current(true)
                        .build());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            framework.setDepartment(dept);
        }

        framework = frameworkRepository.save(framework);

        // Replace requirements cleanly each time the framework is saved
        List<CompetencyRequirement> existing = requirementRepository.findByFrameworkId(framework.getId());
        if (!existing.isEmpty()) {
            requirementRepository.deleteAll(existing);
        }

        RoleCompetencyFramework finalFramework = framework;
        List<CompetencyRequirement> newRequirements = request.getRequirements().stream().map(reqDto -> {
            Skill skill = skillRepository.findById(reqDto.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + reqDto.getSkillId()));
            return CompetencyRequirement.builder()
                    .framework(finalFramework)
                    .skill(skill)
                    .requiredLevel(reqDto.getRequiredLevel())
                    .mandatory(reqDto.isMandatory())
                    .build();
        }).collect(Collectors.toList());

        requirementRepository.saveAll(newRequirements);
        framework.setRequirements(newRequirements);

        return toResponse(framework);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleFrameworkResponse> getAllFrameworks() {
        return frameworkRepository.findByCurrentTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleFrameworkResponse getFrameworkByRoleTitle(String roleTitle) {
        RoleCompetencyFramework framework = frameworkRepository.findByRoleTitleAndCurrentTrue(roleTitle)
                .orElseThrow(() -> new ResourceNotFoundException("No competency framework defined for role: " + roleTitle));
        return toResponse(framework);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleFrameworkResponse getFrameworkById(Long id) {
        RoleCompetencyFramework framework = frameworkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Framework not found: " + id));
        return toResponse(framework);
    }

    @Override
    public void deleteFramework(Long id) {
        if (!frameworkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Framework not found: " + id);
        }
        frameworkRepository.deleteById(id);
    }

    private RoleFrameworkResponse toResponse(RoleCompetencyFramework f) {
        List<CompetencyRequirementDto> reqDtos = requirementRepository.findByFrameworkId(f.getId()).stream()
                .map(r -> CompetencyRequirementDto.builder()
                        .id(r.getId())
                        .skillId(r.getSkill().getId())
                        .skillName(r.getSkill().getName())
                        .requiredLevel(r.getRequiredLevel())
                        .mandatory(r.isMandatory())
                        .build())
                .collect(Collectors.toList());

        return RoleFrameworkResponse.builder()
                .id(f.getId())
                .roleTitle(f.getRoleTitle())
                .departmentId(f.getDepartment() != null ? f.getDepartment().getId() : null)
                .departmentName(f.getDepartment() != null ? f.getDepartment().getName() : null)
                .version(f.getVersion())
                .requirements(reqDtos)
                .createdAt(f.getCreatedAt())
                .build();
    }
}
