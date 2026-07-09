package com.backend.hotelreservationapi.auth_module.service;


import com.backend.hotelreservationapi.auth_module.config.SecurityEnvironment;
import com.backend.hotelreservationapi.auth_module.exception.InvalidOtpException;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SecurityEnvironment securityEnvironment;

    @Async("emailExecutor")
    @Retryable(
            retryFor = Exception.class,
            backoff = @Backoff(delay = 2000, multiplier = 3.0)
    )
    public void sendOtpEmailAsync(String email, String otp) {

        Email from = new Email(securityEnvironment.getFromEmail());
        Email to = new Email(email);

        String subject = "Your OTP Code";
        String contentText = buildMessage(otp);

        Content content = new Content("text/plain", contentText);

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(securityEnvironment.getApiKey());
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            log.info("SendGrid response status: {}", response.getStatusCode());

        } catch (IOException ex) {
            log.error("Failed to send OTP email", ex);
            throw new InvalidOtpException("Failed to send OTP email");
        }
    }

    @Recover
    public void recover(Exception ex, String email, String otp) {
        log.warn("OTP email failed after retries for email={}", email, ex);
    }

    private String buildMessage(String otp) {
        return "Your OTP code is: " + otp +
                "\n\nThis code expires in 5 minutes. Please Check your spam if you can't find the email in your inbox";
    }
}
