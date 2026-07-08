package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.DepartmentGapSummary;
import com.infosys.knowledgegap.dto.EmployeeGapReport;
import com.infosys.knowledgegap.dto.SkillGapDetail;
import com.infosys.knowledgegap.entity.*;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.service.GapAnalysisService;
import com.infosys.knowledgegap.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GapAnalysisServiceImpl implements GapAnalysisService {

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final RoleCompetencyFrameworkRepository frameworkRepository;
    private final CompetencyRequirementRepository requirementRepository;
    private final DepartmentRepository departmentRepository;
    private final RecommendationService recommendationService;

    private static final List<ProficiencyLevel> LEVEL_ORDER = List.of(
            ProficiencyLevel.UNAWARE, ProficiencyLevel.BEGINNER, ProficiencyLevel.INTERMEDIATE,
            ProficiencyLevel.ADVANCED, ProficiencyLevel.EXPERT
    );

    @Override
    public EmployeeGapReport getMyGapReport(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return getGapReportForUser(user.getId());
    }

    @Override
    public EmployeeGapReport getGapReportForUser(Long userId) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        return buildReport(profile);
    }

    @Override
    public List<EmployeeGapReport> getGapReportsForDepartment(Long departmentId) {
        return employeeProfileRepository.findByDepartmentId(departmentId).stream()
                .map(this::buildReport)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeGapReport> getAllGapReports() {
        return employeeProfileRepository.findAll().stream()
                .map(this::buildReport)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentGapSummary> getDepartmentGapSummaries() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream().map(dept -> {
            List<EmployeeGapReport> reports = getGapReportsForDepartment(dept.getId());
            int employeeCount = reports.size();
            double avgReadiness = reports.stream()
                    .filter(EmployeeGapReport::isFrameworkFound)
                    .mapToDouble(EmployeeGapReport::getOverallReadinessPercent)
                    .average().orElse(0.0);
            int totalGaps = reports.stream().mapToInt(EmployeeGapReport::getSkillsWithGap).sum();
            int criticalGaps = reports.stream()
                    .flatMap(r -> r.getGaps() != null ? r.getGaps().stream() : java.util.stream.Stream.empty())
                    .filter(g -> "CRITICAL".equals(g.getSeverity()))
                    .mapToInt(g -> 1).sum();

            return DepartmentGapSummary.builder()
                    .departmentName(dept.getName())
                    .employeeCount(employeeCount)
                    .avgReadinessPercent(Math.round(avgReadiness * 10.0) / 10.0)
                    .totalGaps(totalGaps)
                    .criticalGaps(criticalGaps)
                    .build();
        }).collect(Collectors.toList());
    }

    // ---------- core gap detection logic ----------

    private EmployeeGapReport buildReport(EmployeeProfile profile) {
        User user = profile.getUser();
        String roleTitle = profile.getCurrentRoleTitle();

        Optional<RoleCompetencyFramework> frameworkOpt = roleTitle != null
                ? frameworkRepository.findByRoleTitleAndCurrentTrue(roleTitle)
                : Optional.empty();

        if (frameworkOpt.isEmpty()) {
            return EmployeeGapReport.builder()
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .roleTitle(roleTitle)
                    .departmentName(profile.getDepartment() != null ? profile.getDepartment().getName() : null)
                    .frameworkFound(false)
                    .totalRequiredSkills(0)
                    .skillsMeetingRequirement(0)
                    .skillsWithGap(0)
                    .overallReadinessPercent(0.0)
                    .gaps(List.of())
                    .build();
        }

        RoleCompetencyFramework framework = frameworkOpt.get();
        List<CompetencyRequirement> requirements = requirementRepository.findByFrameworkId(framework.getId());

        Map<Long, EmployeeSkill> employeeSkillsBySkillId = employeeSkillRepository
                .findByEmployeeProfileId(profile.getId()).stream()
                .collect(Collectors.toMap(es -> es.getSkill().getId(), es -> es));

        List<SkillGapDetail> gaps = new ArrayList<>();
        int meetingRequirement = 0;

        for (CompetencyRequirement req : requirements) {
            EmployeeSkill employeeSkill = employeeSkillsBySkillId.get(req.getSkill().getId());
            ProficiencyLevel currentLevel = employeeSkill != null ? employeeSkill.getProficiencyLevel() : null;

            int requiredIndex = LEVEL_ORDER.indexOf(req.getRequiredLevel());
            int currentIndex = currentLevel != null ? LEVEL_ORDER.indexOf(currentLevel) : -1;
            int gapSize = Math.max(0, requiredIndex - currentIndex);

            boolean meets = gapSize == 0;
            if (meets) meetingRequirement++;

            String severity;
            if (gapSize == 0) severity = "NONE";
            else if (gapSize == 1) severity = "MINOR";
            else if (gapSize == 2) severity = "MODERATE";
            else severity = "CRITICAL";

            gaps.add(SkillGapDetail.builder()
                    .skillId(req.getSkill().getId())
                    .skillName(req.getSkill().getName())
                    .requiredLevel(req.getRequiredLevel())
                    .currentLevel(currentLevel)
                    .gapSize(gapSize)
                    .mandatory(req.isMandatory())
                    .severity(severity)
                    .recommendationText(gapSize > 0
                            ? recommendationService.generateRecommendationText(
                                    req.getSkill().getName(), currentLevel, req.getRequiredLevel(), gapSize, severity)
                            : null)
                    .suggestedResources(gapSize > 0
                            ? recommendationService.generateResourceLinks(req.getSkill().getName(), severity)
                            : null)
                    .build());
        }

        int total = requirements.size();
        int withGap = total - meetingRequirement;
        double readiness = total == 0 ? 100.0 : (meetingRequirement * 100.0) / total;

        // Sort worst gaps first for easy scanning on the frontend
        gaps.sort((a, b) -> Integer.compare(b.getGapSize(), a.getGapSize()));

        return EmployeeGapReport.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .roleTitle(roleTitle)
                .departmentName(profile.getDepartment() != null ? profile.getDepartment().getName() : null)
                .frameworkFound(true)
                .totalRequiredSkills(total)
                .skillsMeetingRequirement(meetingRequirement)
                .skillsWithGap(withGap)
                .overallReadinessPercent(Math.round(readiness * 10.0) / 10.0)
                .gaps(gaps)
                .build();
    }
}
