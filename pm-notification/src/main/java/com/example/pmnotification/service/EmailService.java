package com.example.pmnotification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender mailSender;
    private final Environment env;

    public void sendEmail(String to,String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(Objects.requireNonNull(env.getProperty("spring.mail.username")));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка отправки email", e);
        }
    }
}
