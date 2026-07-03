package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificationRequest {

    @NotBlank
    private String name;

    private String issuingBody;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String credentialUrl;
}
