package com.infosys.knowledgegap.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificationResponse {
    private Long id;
    private String name;
    private String issuingBody;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String credentialUrl;
    private boolean expired;
}
