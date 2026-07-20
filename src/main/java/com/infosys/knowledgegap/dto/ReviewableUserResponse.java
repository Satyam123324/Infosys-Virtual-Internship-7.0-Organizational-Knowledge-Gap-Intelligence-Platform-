package com.infosys.knowledgegap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewableUserResponse {
    private Long userId;
    private String fullName;
    private String department;
    private String designation;
}
