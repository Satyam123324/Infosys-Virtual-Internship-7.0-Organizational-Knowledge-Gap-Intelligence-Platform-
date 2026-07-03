package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @Size(max = 100)
    private String fullName;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String designation;

    private String profileImageUrl;
}
