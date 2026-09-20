package com.example.bank.service;

import com.example.bank.event.TransferCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${bank.notification.email}")
    private String notificationEmail;

    @Value("${MAIL_USERNAME}")
    private String mailUsername;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTransferEmail(TransferCompletedEvent event) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(notificationEmail);
        message.setFrom(mailUsername);
        message.setSubject("Перевод выполнен №" + event.getTransferId());

        message.setText(
                "Перевод успешно выполнен.\n\n" +
                        "Номер перевода: " + event.getTransferId() + "\n" +
                        "Отправитель: " + event.getFromOwner() + "\n" +
                        "Получатель: " + event.getToOwner() + "\n" +
                        "Сумма: " + event.getAmount() + "\n" +
                        "Дата: " + event.getOccurredAt()
        );

        mailSender.send(message);

        log.info("Email успешно отправлен: transferId={}, recipient={}",
                event.getTransferId(),
                notificationEmail);
    }
}