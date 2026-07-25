package com.infosys.knowledgegap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // When true (dev only), the OTP is printed to the console so the password
    // reset / change flow can be tested locally without a real SMTP server.
    // Keep this false in production — it must never log real verification codes.
    @Value("${app.otp.log-to-console:false}")
    private boolean logOtpToConsole;

    @Async
    public void sendOtpEmail(String toEmail, String otp, String purposeLabel, int validMinutes) {
        if (logOtpToConsole) {
            log.warn("=== DEV OTP for {} ({}): {}  [valid {} min] ===", toEmail, purposeLabel, otp, validMinutes);
        }

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

    /**
     * Generic delivery channel for the Notification module. SMS/push are stubbed
     * for now (see NotificationServiceImpl) — this is the only channel that
     * actually sends. Runs async so a slow/broken SMTP server never blocks the
     * request that triggered the notification (e.g. the nightly scheduler sweep).
     */
    @Async
    public void sendNotificationEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body + "\n\n— Knowledge Gap Intelligence Platform\n"
                    + "You can manage or dismiss this in your Notification Center.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send notification email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
