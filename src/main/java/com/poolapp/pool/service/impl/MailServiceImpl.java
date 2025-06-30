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

    private final JavaMailSender javaMailSender;


    @Value("${spring.booking-confirmation.subject}")
    private String bookingConfirmationSubject;

    @Value("${spring.booking-confirmation.body}")
    private String bookingConfirmationBody;

    @Override
    public void sendBookingConfirmationEmail(String toEmail, SessionDTO sessionDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(bookingConfirmationSubject);
        message.setText(String.format(bookingConfirmationBody,
                sessionDTO.getPoolName(),
                sessionDTO.getStartTime().toString()));
        javaMailSender.send(message);
    }
}

