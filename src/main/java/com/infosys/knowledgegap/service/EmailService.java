package com.infosys.knowledgegap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp, String purposeLabel, int validMinutes) {
        String body = "Your one-time verification code for the Knowledge Gap Intelligence Platform is:\n\n"
                + "        " + otp + "\n\n"
                + "This code is for: " + purposeLabel + "\n"
                + "It expires in " + validMinutes + " minutes and can only be used once.\n\n"
                + "If you did not request this, you can safely ignore this email.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Verification Code: " + otp);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String body = "Hi " + fullName + ",\n\n"
                + "Welcome to the Organizational Knowledge Gap Intelligence Platform. "
                + "Your account has been created successfully.\n\n"
                + "Team Infosys";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Welcome to the Knowledge Gap Intelligence Platform");
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
