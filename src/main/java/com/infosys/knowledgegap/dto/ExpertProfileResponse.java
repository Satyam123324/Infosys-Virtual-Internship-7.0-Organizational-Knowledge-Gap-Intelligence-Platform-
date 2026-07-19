package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertProfileResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String designation;
    private String skillName;
    private ProficiencyLevel proficiencyLevel;
}
