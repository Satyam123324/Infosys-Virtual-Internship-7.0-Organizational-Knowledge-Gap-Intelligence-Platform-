package com.infosys.knowledgegap.service.impl;

import com.infosys.knowledgegap.dto.*;
import com.infosys.knowledgegap.entity.*;
import com.infosys.knowledgegap.enums.AssessmentType;
import com.infosys.knowledgegap.exception.ResourceNotFoundException;
import com.infosys.knowledgegap.repository.*;
import com.infosys.knowledgegap.service.EmployeeProfileService;
import com.infosys.knowledgegap.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final CertificationRepository certificationRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final SkillRepository skillRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // ---------- Profile ----------

    @Override
    public EmployeeProfileResponse getOrCreateMyProfile(String email) {
        User user = findUserByEmail(email);
        EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> employeeProfileRepository.save(
                        EmployeeProfile.builder().user(user).build()));
        return toResponse(profile);
    }

    @Override
    public EmployeeProfileResponse updateMyProfile(String email, EmployeeProfileRequest request) {
        User user = findUserByEmail(email);
        EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> EmployeeProfile.builder().user(user).build());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            profile.setDepartment(dept);
        }
        if (request.getCurrentRoleTitle() != null) profile.setCurrentRoleTitle(request.getCurrentRoleTitle());
        if (request.getEmploymentType() != null) profile.setEmploymentType(request.getEmploymentType());
        if (request.getDateOfJoining() != null) profile.setDateOfJoining(request.getDateOfJoining());

        return toResponse(employeeProfileRepository.save(profile));
    }

    @Override
    public EmployeeProfileResponse updateEmployeeProfileAsAdmin(Long userId, EmployeeProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> EmployeeProfile.builder().user(user).build());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            profile.setDepartment(dept);
        }
        if (request.getCurrentRoleTitle() != null) profile.setCurrentRoleTitle(request.getCurrentRoleTitle());
        if (request.getEmploymentType() != null) profile.setEmploymentType(request.getEmploymentType());
        if (request.getDateOfJoining() != null) profile.setDateOfJoining(request.getDateOfJoining());

        return toResponse(employeeProfileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeProfileResponse getProfileByUserId(Long userId) {
        EmployeeProfile profile = employeeProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user: " + userId));
        return toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeProfileResponse> getAllProfiles() {
        return employeeProfileRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeProfileResponse> getProfilesByDepartment(Long departmentId) {
        return employeeProfileRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ---------- Skills ----------

    @Override
    public EmployeeSkillResponse addOrUpdateSkill(String email, EmployeeSkillRequest request) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.getSkillId()));

        EmployeeSkill employeeSkill = employeeSkillRepository
                .findByEmployeeProfileIdAndSkillId(profile.getId(), skill.getId())
                .orElse(EmployeeSkill.builder().employeeProfile(profile).skill(skill).build());

        employeeSkill.setProficiencyLevel(request.getProficiencyLevel());
        employeeSkill.setAssessmentType(AssessmentType.SELF);
        employeeSkill.setSelfRating(request.getSelfRating());
        employeeSkill.setLastAssessedDate(LocalDate.now());

        return toSkillResponse(employeeSkillRepository.save(employeeSkill));
    }

    @Override
    public List<EmployeeSkillResponse> getMySkills(String email) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        return employeeSkillRepository.findByEmployeeProfileId(profile.getId()).stream()
                .map(this::toSkillResponse).collect(Collectors.toList());
    }

    @Override
    public void removeSkill(String email, Long skillId) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        employeeSkillRepository.deleteByEmployeeProfileIdAndSkillId(profile.getId(), skillId);
    }

    // ---------- Certifications ----------

    @Override
    public CertificationResponse addCertification(String email, CertificationRequest request) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        Certification cert = Certification.builder()
                .employeeProfile(profile)
                .name(request.getName())
                .issuingBody(request.getIssuingBody())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .credentialUrl(request.getCredentialUrl())
                .build();
        return toCertResponse(certificationRepository.save(cert));
    }

    @Override
    public List<CertificationResponse> getMyCertifications(String email) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        return certificationRepository.findByEmployeeProfileId(profile.getId()).stream()
                .map(this::toCertResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteCertification(String email, Long certificationId) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        Certification cert = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        if (!cert.getEmployeeProfile().getId().equals(profile.getId())) {
            throw new ResourceNotFoundException("Certification does not belong to current user");
        }
        certificationRepository.deleteById(certificationId);
    }

    // ---------- Work Experience ----------

    @Override
    public WorkExperienceResponse addWorkExperience(String email, WorkExperienceRequest request) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        WorkExperience exp = WorkExperience.builder()
                .employeeProfile(profile)
                .companyOrProject(request.getCompanyOrProject())
                .roleTitle(request.getRoleTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .build();
        return toExpResponse(workExperienceRepository.save(exp));
    }

    @Override
    public List<WorkExperienceResponse> getMyWorkExperience(String email) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        return workExperienceRepository.findByEmployeeProfileId(profile.getId()).stream()
                .map(this::toExpResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteWorkExperience(String email, Long workExperienceId) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        WorkExperience exp = workExperienceRepository.findById(workExperienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Work experience not found"));
        if (!exp.getEmployeeProfile().getId().equals(profile.getId())) {
            throw new ResourceNotFoundException("Work experience does not belong to current user");
        }
        workExperienceRepository.deleteById(workExperienceId);
    }

    // ---------- helpers ----------

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private EmployeeProfile getOrCreateProfileEntity(String email) {
        User user = findUserByEmail(email);
        return employeeProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> employeeProfileRepository.save(
                        EmployeeProfile.builder().user(user).build()));
    }

    private EmployeeProfileResponse toResponse(EmployeeProfile p) {
        return EmployeeProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .fullName(p.getUser().getFullName())
                .email(p.getUser().getEmail())
                .departmentId(p.getDepartment() != null ? p.getDepartment().getId() : null)
                .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
                .currentRoleTitle(p.getCurrentRoleTitle())
                .profileImageUrl(p.getUser().getProfileImageUrl())
                .resumeUrl(p.getResumeUrl())
                .employmentType(p.getEmploymentType())
                .dateOfJoining(p.getDateOfJoining())
                .skills(p.getSkills().stream().map(this::toSkillResponse).collect(Collectors.toList()))
                .certifications(p.getCertifications().stream().map(this::toCertResponse).collect(Collectors.toList()))
                .workExperience(p.getWorkExperience().stream().map(this::toExpResponse).collect(Collectors.toList()))
                .build();
    }

    private EmployeeSkillResponse toSkillResponse(EmployeeSkill es) {
        return EmployeeSkillResponse.builder()
                .id(es.getId())
                .skillId(es.getSkill().getId())
                .skillName(es.getSkill().getName())
                .categoryName(es.getSkill().getCategory() != null ? es.getSkill().getCategory().getName() : null)
                .proficiencyLevel(es.getProficiencyLevel())
                .assessmentType(es.getAssessmentType())
                .lastAssessedDate(es.getLastAssessedDate())
                .selfRating(es.getSelfRating())
                .peerRating(es.getPeerRating())
                .managerRating(es.getManagerRating())
                .build();
    }

    private CertificationResponse toCertResponse(Certification c) {
        boolean expired = c.getExpiryDate() != null && c.getExpiryDate().isBefore(LocalDate.now());
        return CertificationResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .issuingBody(c.getIssuingBody())
                .issueDate(c.getIssueDate())
                .expiryDate(c.getExpiryDate())
                .credentialUrl(c.getCredentialUrl())
                .expired(expired)
                .build();
    }

    private WorkExperienceResponse toExpResponse(WorkExperience w) {
        return WorkExperienceResponse.builder()
                .id(w.getId())
                .companyOrProject(w.getCompanyOrProject())
                .roleTitle(w.getRoleTitle())
                .startDate(w.getStartDate())
                .endDate(w.getEndDate())
                .description(w.getDescription())
                .build();
    }

    @Override
    public String uploadProfilePhoto(String email, org.springframework.web.multipart.MultipartFile file) {
        User user = findUserByEmail(email);
        try {
            String url = fileStorageService.storeProfilePhoto(file, user.getId());
            user.setProfileImageUrl(url);
            userRepository.save(user);
            return url;
        } catch (java.io.IOException ex) {
            throw new RuntimeException("Failed to store profile photo: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String uploadResume(String email, org.springframework.web.multipart.MultipartFile file) {
        EmployeeProfile profile = getOrCreateProfileEntity(email);
        try {
            String url = fileStorageService.storeResume(file, profile.getUser().getId());
            profile.setResumeUrl(url);
            employeeProfileRepository.save(profile);
            return url;
        } catch (java.io.IOException ex) {
            throw new RuntimeException("Failed to store resume: " + ex.getMessage(), ex);
        }
    }
}
