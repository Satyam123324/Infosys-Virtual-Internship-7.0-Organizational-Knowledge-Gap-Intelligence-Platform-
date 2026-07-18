package com.infosys.knowledgegap.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ColleagueDto {
    private Long userId;
    private String fullName;
    private String departmentName;
    private String currentRoleTitle;
}
