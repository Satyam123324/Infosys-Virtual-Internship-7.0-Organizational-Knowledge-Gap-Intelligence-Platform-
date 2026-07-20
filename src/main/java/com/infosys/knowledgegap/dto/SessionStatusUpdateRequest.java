package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private SessionStatus status;
}
