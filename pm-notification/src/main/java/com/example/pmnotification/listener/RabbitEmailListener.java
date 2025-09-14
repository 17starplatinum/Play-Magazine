package com.example.pmnotification.listener;

import com.example.pmnotification.dto.EmailMessageDto; // ← создай этот класс в email-service!
import com.example.pmnotification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitEmailListener {

    private final EmailService emailService;

    @RabbitListener(queues = "email-notification")
    public void receiveMessage(EmailMessageDto message) {
        log.info("Получено сообщение: {}", message);

        if (message == null || message.getTo() == null) {
            log.warn("Пустое сообщение получено");
            return;
        }

        try {
            emailService.sendEmail(message.getTo(), message.getSubject(), message.getText());
            log.info("Email успешно отправлен на адрес: {}", message.getTo());
        } catch (Exception e) {
            log.error("Ошибка при отправке email на адрес: {}", message.getTo(), e);
        }
    }
}