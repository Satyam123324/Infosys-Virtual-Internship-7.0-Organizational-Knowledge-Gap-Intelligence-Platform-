package com.infosys.knowledgegap.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain uppercase, lowercase, digit and special character"
    )
    private String newPassword;
}
