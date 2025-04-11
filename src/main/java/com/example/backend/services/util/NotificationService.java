package com.example.backend.services.util;

import com.example.backend.exceptions.accepted.EmailSendingException;
import com.example.backend.model.auth.User;
import com.example.backend.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final Environment env;

    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setFrom(env.getProperty("spring.mail.username"));
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(text, true);

            mailSender.send(message);
            log.info("Письмо отправлено {}-у", to);

        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма {}-у: {}", to, e.getMessage());
            throw new EmailSendingException("Ошибка при отправке письма:", e);
        }
    }

    public void notifyUserAboutAuthorRequestApproval(User user, String approvedBy) {
        String subject = "Ваша заявка на автора одобрена";
        String content = """
                <html>
                    <body>
                        <h2>Поздравляем, %s!</h2>
                        <p>Ваша заявка на получение статуса автора была одобрена администратором %s.</p>
                        <p>Теперь вы можете публиковать свои приложения в нашем магазине.</p>
                        <p>С уважением,<br>Команда PlayMagazine</p>
                    </body>
                </html>
                """.formatted(user.getUsername(), approvedBy);

        sendEmail(user.getEmail(), subject, content);
    }

    public void notifyUserAboutAuthorRequestRejection(User user, String rejectedBy, String reason) {
        String subject = "Результат рассмотрения вашей заявки на автора";
        String content = """
                <html>
                    <body>
                        <h2>Уважаемый %s,</h2>
                        <p>К сожалению, ваша заявка на получение статуса автора была отклонена администратором %s.</p>
                        <p><strong>Причина:</strong> %s</p>
                        <p>Вы можете подать новую заявку через 30 дней, исправив указанные замечания.</p>
                        <p>С уважением,<br>Команда AppStore</p>
                    </body>
                </html>
                """.formatted(user.getUsername(), rejectedBy, reason);

        sendEmail(user.getEmail(), subject, content);
    }

    public void notifyAdminsAboutNewAuthorRequest(User user) {
        String adminEmails = String.join(",", userRepository.findAdminEmails());

        String subject = "Новая заявка на автора";
        String content = """
                <html>
                    <body>
                        <h2>Новая заявка на автора</h2>
                        <p>Пользователь %s (%s) подал заявку на получение статуса автора.</p>
                        <p><a href="%s/admin/author-requests/%s">Перейти к рассмотрению заявки</a></p>
                    </body>
                </html>
                """.formatted(
                user.getUsername(),
                user.getEmail(),
                env.getProperty("app.frontend.admin-panel-url"),
                user.getId()
        );

        sendEmail(adminEmails, subject, content);
    }
}
