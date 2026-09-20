package com.example.bank.service;

import com.example.bank.event.TransferCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService service;

    @BeforeEach
    void setUp() {

        service = new EmailService(mailSender);

        ReflectionTestUtils.setField(
                service,
                "notificationEmail",
                "bank@example.com"
        );

        ReflectionTestUtils.setField(
                service,
                "mailUsername",
                "smtp@example.com"
        );
    }


    // =========================================================
    // sendTransferEmail()
    // =========================================================

    @Test
    void shouldSendTransferEmail() {

        Long transferId = 123L;

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(transferId);

        when(event.getFromOwner())
                .thenReturn("Alice");

        when(event.getToOwner())
                .thenReturn("Bob");

        when(event.getAmount())
                .thenReturn(new BigDecimal("1500.00"));

        LocalDateTime occurredAt =
                LocalDateTime.of(2026, 9, 19, 12, 30);

        when(event.getOccurredAt())
                .thenReturn(occurredAt);

        service.sendTransferEmail(event);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }


    @Test
    void shouldBuildEmailWithCorrectRecipient() {

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(123L);

        when(event.getFromOwner())
                .thenReturn("Alice");

        when(event.getToOwner())
                .thenReturn("Bob");

        when(event.getAmount())
                .thenReturn(new BigDecimal("1500.00"));

        LocalDateTime occurredAt =
                LocalDateTime.of(2026, 9, 19, 12, 30);

        when(event.getOccurredAt())
                .thenReturn(occurredAt);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        service.sendTransferEmail(event);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertArrayEquals(
                new String[]{"bank@example.com"},
                message.getTo()
        );
    }


    @Test
    void shouldBuildEmailWithCorrectSender() {

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(123L);

        when(event.getFromOwner())
                .thenReturn("Alice");

        when(event.getToOwner())
                .thenReturn("Bob");

        when(event.getAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(event.getOccurredAt())
                .thenReturn(LocalDateTime.of(
                        2026,
                        9,
                        19,
                        12,
                        30
                ));

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        service.sendTransferEmail(event);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(
                "smtp@example.com",
                message.getFrom()
        );
    }


    @Test
    void shouldBuildEmailWithCorrectSubject() {

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(123L);

        when(event.getFromOwner())
                .thenReturn("Alice");

        when(event.getToOwner())
                .thenReturn("Bob");

        when(event.getAmount())
                .thenReturn(new BigDecimal("1500.00"));

        when(event.getOccurredAt())
                .thenReturn(LocalDateTime.of(
                        2026,
                        9,
                        19,
                        12,
                        30
                ));

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        service.sendTransferEmail(event);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(
                "Перевод выполнен №123",
                message.getSubject()
        );
    }


    @Test
    void shouldBuildEmailWithCorrectText() {

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(123L);

        when(event.getFromOwner())
                .thenReturn("Alice");

        when(event.getToOwner())
                .thenReturn("Bob");

        when(event.getAmount())
                .thenReturn(new BigDecimal("1500.00"));

        LocalDateTime occurredAt =
                LocalDateTime.of(2026, 9, 19, 12, 30);

        when(event.getOccurredAt())
                .thenReturn(occurredAt);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        service.sendTransferEmail(event);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        String expectedText =
                "Перевод успешно выполнен.\n\n" +
                        "Номер перевода: 123\n" +
                        "Отправитель: Alice\n" +
                        "Получатель: Bob\n" +
                        "Сумма: 1500.00\n" +
                        "Дата: 2026-09-19T12:30";

        assertEquals(
                expectedText,
                message.getText()
        );
    }


    @Test
    void shouldUseAllEventDataWhenBuildingEmail() {

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(999L);

        when(event.getFromOwner())
                .thenReturn("Ivan");

        when(event.getToOwner())
                .thenReturn("Petr");

        when(event.getAmount())
                .thenReturn(new BigDecimal("25000.75"));

        LocalDateTime occurredAt =
                LocalDateTime.of(
                        2026,
                        9,
                        20,
                        18,
                        45
                );

        when(event.getOccurredAt())
                .thenReturn(occurredAt);

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        service.sendTransferEmail(event);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(
                "Перевод выполнен №999",
                message.getSubject()
        );

        assertTrue(
                message.getText().contains("Номер перевода: 999")
        );

        assertTrue(
                message.getText().contains("Отправитель: Ivan")
        );

        assertTrue(
                message.getText().contains("Получатель: Petr")
        );

        assertTrue(
                message.getText().contains("Сумма: 25000.75")
        );

        assertTrue(
                message.getText().contains("Дата: 2026-09-20T18:45")
        );
    }


    @Test
    void shouldSendEmailExactlyOnce() {

        TransferCompletedEvent event = mock(TransferCompletedEvent.class);

        when(event.getTransferId())
                .thenReturn(123L);

        when(event.getFromOwner())
                .thenReturn("Alice");

        when(event.getToOwner())
                .thenReturn("Bob");

        when(event.getAmount())
                .thenReturn(new BigDecimal("100.00"));

        when(event.getOccurredAt())
                .thenReturn(LocalDateTime.of(
                        2026,
                        9,
                        19,
                        10,
                        0
                ));

        service.sendTransferEmail(event);

        verify(mailSender, times(1))
                .send(any(SimpleMailMessage.class));
    }
}