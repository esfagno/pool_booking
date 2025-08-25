package com.poolapp.pool.service.impl;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.booking-confirmation.subject}")
    private String subjectTemplate;

    @Value("${spring.booking-confirmation.body}")
    private String bodyTemplate;

    @Override
    public void sendBookingConfirmationEmail(String toEmail, SessionDTO sessionDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(formatTemplate(subjectTemplate, sessionDTO));
        message.setText(formatTemplate(bodyTemplate, sessionDTO));
        mailSender.send(message);
    }

    private String formatTemplate(String template, SessionDTO sessionDTO) {
        return template
                .replace("{poolName}", sessionDTO.getPoolName())
                .replace("{startTime}", sessionDTO.getStartTime().toString());
    }
}