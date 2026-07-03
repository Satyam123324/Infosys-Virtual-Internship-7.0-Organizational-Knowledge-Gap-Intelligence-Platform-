package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.EmploymentType;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeProfileRequest {
    private Long departmentId;
    private String currentRoleTitle;
    private EmploymentType employmentType;
    private LocalDate dateOfJoining;
}
