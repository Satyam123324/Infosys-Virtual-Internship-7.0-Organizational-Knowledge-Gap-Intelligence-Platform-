package com.infosys.knowledgegap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingEnrollmentResponse {
    private Long id;
    private String courseName;
    private String provider;
    private LocalDate deadline;
    private int progressPercent;
    private boolean completed;
    private boolean overdue;
    private LocalDateTime enrolledAt;
}
