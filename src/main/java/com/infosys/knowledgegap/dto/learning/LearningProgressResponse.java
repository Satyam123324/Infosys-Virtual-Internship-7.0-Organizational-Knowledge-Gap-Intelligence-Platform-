package com.infosys.knowledgegap.dto.learning;

import com.infosys.knowledgegap.enums.ProgressStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressResponse {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private Long courseId;

    private String courseTitle;

    private ProgressStatus status;

    private Integer progressPercentage;

    private LocalDate enrolledDate;

    private LocalDate completedDate;
}