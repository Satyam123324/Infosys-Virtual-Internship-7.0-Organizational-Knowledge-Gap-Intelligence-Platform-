package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorshipSessionResponse {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private Long menteeId;
    private String menteeName;
    private String topic;
    private LocalDateTime scheduledAt;
    private SessionStatus status;
    private String notes;
    // Lets the frontend know if the logged-in user is the mentor or mentee on this session,
    // so it can render the right actions/labels without re-deriving it from raw IDs.
    private boolean isMentor;
}
