package com.infosys.knowledgegap.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationSummary {
    private long unreadCount;
    private List<NotificationResponse> notifications;
}
