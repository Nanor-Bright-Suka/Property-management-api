package com.backend.hotelreservationapi.auth_module.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async("emailExecutor")
    @Retryable(
            retryFor = MailSendException.class,
            backoff = @Backoff(delay = 2000, multiplier = 3.0)
    )
    public void sendOtpEmailAsync(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText(buildMessage(otp));
        mailSender.send(message);

        log.info("Email sent to: {}", email);
    }

    @Recover
    public void recover(MailSendException ex, String email, String otp) {
        log.warn("OTP email send failed after retries for email={}", email, ex);
    }

    private String buildMessage(String otp) {
        return "Your OTP code is: " + otp +
                "\n\nThis code expires in 5 minutes.";
    }
}
