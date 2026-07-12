package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8)
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain uppercase, lowercase, digit and special character"
    )
    private String newPassword;
}
