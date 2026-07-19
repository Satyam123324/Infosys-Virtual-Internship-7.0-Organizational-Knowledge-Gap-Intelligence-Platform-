package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.ReviewableUserResponse;
import com.infosys.knowledgegap.dto.SkillAssessmentSubmissionRequest;
import com.infosys.knowledgegap.dto.SkillAssessmentSubmissionResponse;
import com.infosys.knowledgegap.entity.*;
import com.infosys.knowledgegap.enums.AssessmentType;
import com.infosys.knowledgegap.enums.ProficiencyLevel;
import com.infosys.knowledgegap.enums.RoleType;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.service.SkillAssessmentSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillAssessmentSubmissionServiceImpl implements SkillAssessmentSubmissionService {

    // Roles allowed to submit a MANAGER-type rating. There's no reporting-line
    // (manager-of) relationship in the schema yet, so this is role-gated rather
    // than restricted to a specific person's actual manager — documented
    // simplification, see class-level notes in the controller.
    private static final Set<RoleType> MANAGER_TIER_ROLES = Set.of(
            RoleType.TEAM_LEAD_MANAGER, RoleType.DEPARTMENT_HEAD,
            RoleType.HR_SPECIALIST, RoleType.LEARNING_DEVELOPMENT_ADMIN, RoleType.SYSTEM_ADMINISTRATOR);

    private final SkillAssessmentSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    @Override
    public SkillAssessmentSubmissionResponse submit(String email, SkillAssessmentSubmissionRequest request) {
        User assessor = getUser(email);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        User assessedUser = resolveAndValidateAssessedUser(assessor, request);

        SkillAssessmentSubmission submission = submissionRepository.save(SkillAssessmentSubmission.builder()
                .assessedUser(assessedUser)
                .assessor(assessor)
                .skill(skill)
                .type(request.getType())
                .rating(request.getRating())
                .comments(request.getComments())
                .build());

        mirrorOntoEmployeeSkill(assessedUser, skill, request.getType(), request.getRating());

        return toResponse(submission);
    }

    @Override
    public List<SkillAssessmentSubmissionResponse> getReceivedByMe(String email) {
        User user = getUser(email);
        return submissionRepository.findByAssessedUserIdOrderBySubmittedAtDesc(user.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SkillAssessmentSubmissionResponse> getSubmittedByMe(String email) {
        User user = getUser(email);
        return submissionRepository.findByAssessorIdOrderBySubmittedAtDesc(user.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ReviewableUserResponse> getReviewableUsers(String email) {
        User user = getUser(email);
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .map(u -> ReviewableUserResponse.builder()
                        .userId(u.getId())
                        .fullName(u.getFullName())
                        .department(u.getDepartment())
                        .designation(u.getDesignation())
                        .build())
                .collect(Collectors.toList());
    }

    // ---------- helpers ----------

    private User resolveAndValidateAssessedUser(User assessor, SkillAssessmentSubmissionRequest request) {
        switch (request.getType()) {
            case SELF:
                // Force self-assessments to actually be about the caller, regardless of what was sent.
                return assessor;

            case PEER: {
                if (request.getAssessedUserId().equals(assessor.getId())) {
                    throw new IllegalArgumentException("Use a self-assessment to rate your own skills");
                }
                return userRepository.findById(request.getAssessedUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            }

            case MANAGER: {
                if (request.getAssessedUserId().equals(assessor.getId())) {
                    throw new IllegalArgumentException("Use a self-assessment to rate your own skills");
                }
                boolean isManagerTier = assessor.getRoles().stream()
                        .anyMatch(r -> MANAGER_TIER_ROLES.contains(r.getName()));
                if (!isManagerTier) {
                    throw new IllegalArgumentException("Only manager/HR/admin roles can submit a manager assessment");
                }
                return userRepository.findById(request.getAssessedUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            }

            default:
                throw new IllegalArgumentException("Unsupported assessment type");
        }
    }

    /**
     * Mirrors the latest rating of each type onto EmployeeSkill's
     * selfRating/peerRating/managerRating fields for quick display elsewhere
     * (e.g. a skill card showing all three side by side). Only a SELF
     * submission moves the official proficiencyLevel used by gap analysis —
     * peer/manager input is informational context, not authoritative, so it
     * doesn't silently change what the gap-analysis engine sees.
     */
    private void mirrorOntoEmployeeSkill(User assessedUser, Skill skill, AssessmentType type, Integer rating) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(assessedUser.getId()).orElse(null);
        if (profile == null) return; // no profile yet — submission is still recorded, just nothing to mirror onto

        EmployeeSkill employeeSkill = employeeSkillRepository
                .findByEmployeeProfileIdAndSkillId(profile.getId(), skill.getId())
                .orElseGet(() -> EmployeeSkill.builder()
                        .employeeProfile(profile)
                        .skill(skill)
                        .proficiencyLevel(ProficiencyLevel.UNAWARE)
                        .build());

        switch (type) {
            case SELF:
                employeeSkill.setSelfRating(rating);
                employeeSkill.setProficiencyLevel(ratingToProficiency(rating));
                employeeSkill.setAssessmentType(AssessmentType.SELF);
                break;
            case PEER:
                employeeSkill.setPeerRating(rating);
                break;
            case MANAGER:
                employeeSkill.setManagerRating(rating);
                break;
        }

        employeeSkillRepository.save(employeeSkill);
    }

    private ProficiencyLevel ratingToProficiency(int rating) {
        return switch (rating) {
            case 1 -> ProficiencyLevel.UNAWARE;
            case 2 -> ProficiencyLevel.BEGINNER;
            case 3 -> ProficiencyLevel.INTERMEDIATE;
            case 4 -> ProficiencyLevel.ADVANCED;
            default -> ProficiencyLevel.EXPERT;
        };
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SkillAssessmentSubmissionResponse toResponse(SkillAssessmentSubmission s) {
        return SkillAssessmentSubmissionResponse.builder()
                .id(s.getId())
                .assessedUserId(s.getAssessedUser().getId())
                .assessedUserName(s.getAssessedUser().getFullName())
                .assessorId(s.getAssessor().getId())
                .assessorName(s.getAssessor().getFullName())
                .skillId(s.getSkill().getId())
                .skillName(s.getSkill().getName())
                .type(s.getType())
                .rating(s.getRating())
                .comments(s.getComments())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}
