package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.EmploymentType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private Long departmentId;
    private String departmentName;
    private String currentRoleTitle;
    private EmploymentType employmentType;
    private LocalDate dateOfJoining;
    private List<EmployeeSkillResponse> skills;
    private List<CertificationResponse> certifications;
    private List<WorkExperienceResponse> workExperience;
}
