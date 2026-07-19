package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MentorshipSessionRequest {

    @NotNull(message = "Mentor is required")
    private Long mentorId;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Session must be scheduled in the future")
    private LocalDateTime scheduledAt;
}
