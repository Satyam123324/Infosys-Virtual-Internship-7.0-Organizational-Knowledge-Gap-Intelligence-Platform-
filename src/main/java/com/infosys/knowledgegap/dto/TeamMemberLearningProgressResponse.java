package com.infosys.knowledgegap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberLearningProgressResponse {
    private Long userId;
    private String fullName;
    private String department;
    private String designation;
    private int totalEnrollments;
    private int completedCount;
    private int overdueCount;
    private double avgProgressPercent;
    private int milestonesEarned;
}
