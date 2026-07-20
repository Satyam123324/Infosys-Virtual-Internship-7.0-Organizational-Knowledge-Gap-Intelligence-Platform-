package com.infosys.knowledgegap.dto.learning;

import com.infosys.knowledgegap.enums.ProgressStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningProgressRequest {

    private Long employeeId;

    private Long courseId;

    private ProgressStatus status;

    private Integer progressPercentage;
}