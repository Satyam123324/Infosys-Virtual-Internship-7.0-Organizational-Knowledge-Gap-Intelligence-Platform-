package com.infosys.knowledgegap.service;

import com.infosys.knowledgegap.dto.*;
import java.util.List;

public interface EmployeeProfileService {
    EmployeeProfileResponse getOrCreateMyProfile(String email);
    EmployeeProfileResponse updateMyProfile(String email, EmployeeProfileRequest request);
    EmployeeProfileResponse getProfileByUserId(Long userId);
    List<EmployeeProfileResponse> getAllProfiles();
    List<EmployeeProfileResponse> getProfilesByDepartment(Long departmentId);

    EmployeeSkillResponse addOrUpdateSkill(String email, EmployeeSkillRequest request);
    List<EmployeeSkillResponse> getMySkills(String email);
    void removeSkill(String email, Long skillId);

    CertificationResponse addCertification(String email, CertificationRequest request);
    List<CertificationResponse> getMyCertifications(String email);
    void deleteCertification(String email, Long certificationId);

    WorkExperienceResponse addWorkExperience(String email, WorkExperienceRequest request);
    List<WorkExperienceResponse> getMyWorkExperience(String email);
    void deleteWorkExperience(String email, Long workExperienceId);
}
