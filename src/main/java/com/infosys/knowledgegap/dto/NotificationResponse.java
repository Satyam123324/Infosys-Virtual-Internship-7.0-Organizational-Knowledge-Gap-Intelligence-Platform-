package com.infosys.knowledgegap.dto;

import com.infosys.knowledgegap.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private Long referenceId;
    private LocalDateTime createdAt;
}
