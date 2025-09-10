package com.example.backend.services.util;

import com.example.backend.dto.EmailMessageDto;
import com.example.backend.exceptions.accepted.EmailSendingException;
import com.example.backend.model.auth.User;
import com.example.backend.model.data.subscriptions.Subscription;
import com.example.backend.repositories.auth.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;

    @Value("${spring.jms.template.default-destination}")
    private String emailQueue;


    public void notifyUserAboutAuthorRequestApproval(User user, String approvedBy, String role) {
        String message = switch (role) {
            case "ADMIN" -> {
                role = "админа";
                yield "Теперь вы имеете право свободно управлять ролями всех других пользователей.";
            }
            case "MODERATOR" -> {
                role = "модератора";
                yield "Теперь вы имеете право управлять запросами на повышение роли, а также удалить приложения.";
            }
            case "DEVELOPER" -> {
                role = "автора";
                yield "Теперь вы можете публиковать свои приложения в нашем магазине.";
            }
            case "USER" -> {
                role = "пользователя";
                yield "Теперь вы можете скачать приложения и взаимодействовать с ними.";
            }
            default -> {
                role = "";
                yield "";
            }
        };

        String subject = "Ваша заявка на %s одобрена".formatted(role);
        String content = """
                <html>
                    <body>
                        <h2>Поздравляем, %s!</h2>
                        <p>Ваша заявка на получение статуса автора была одобрена администратором %s.</p>
                        <p>%s</p>
                        <p>С уважением,<br>Команда PlayMagazine</p>
                    </body>
                </html>
                """.formatted(user.getUsername(), approvedBy, message);

        sendEmailAsync(user.getEmail(), subject, content);
    }

    public void notifyUserAboutAuthorRequestRejection(User user, String rejectedBy, String reason, String role) {
        String subject = "Результат рассмотрения вашей заявки на %s".formatted(role);
        String content = """
                <html>
                    <body>
                        <h2>Уважаемый %s,</h2>
                        <p>К сожалению, ваша заявка на получение статуса %s была отклонена администратором %s.</p>
                        <p><strong>Причина:</strong> %s</p>
                        <p>Вы можете подать новую заявку через 30 дней, исправив указанные замечания.</p>
                        <p>С уважением,<br>Команда PlayMagazine</p>
                    </body>
                </html>
                """.formatted(user.getUsername(), role, rejectedBy, reason);

        sendEmailAsync(user.getEmail(), subject, content);
    }

    public void notifyAdminsAboutNewAuthorRequest(User user) {
        String adminEmails = String.join(",", userRepository.findAdminEmails());

        String subject = "Новая заявка на автора";
        String content = """
                <html>
                    <body>
                        <h2>Новая заявка на автора</h2>
                        <p>Пользователь %s (%s) подал заявку на получение статуса автора.</p>
                    </body>
                </html>
                """.formatted(user.getUsername(), user.getEmail());

        sendEmailAsync(adminEmails, subject, content);
    }

    public void notifyUserAboutSubscriptionCancellation(User user, Subscription subscription) {
        String subject = "Отмена подписки";
        String content = """
                 <html>
                     <body>
                     <h2>Уважаемый %s,</h2>
                     <p>Вы отменили подписку %s на приложении %s.</p>
                     <p>С уважением,<br>Команда PlayMagazine</p>
                     </body>
                 </html>
                """.formatted(user.getUsername(), subscription.getName(), subscription.getApp().getName());

        sendEmailAsync(user.getEmail(), subject, content);
    }

    public void notifyUserAboutSubscriptionAutoRenewal(User user, Subscription subscription, boolean state) {
        String subject = "Изменение автопродления подписки";
        String action = (state) ? "подключили" : "отключили";
        String content = """
                <html>
                    <body>
                    <h2>Уважаемый %s,</h2>
                    <p>Вы %s автоматическое продление подписки %s на приложении %s.</p>
                    <p>С уважением,<br>Команда PlayMagazine</p>
                    </body>
                </html>
                """.formatted(user.getUsername(), action, subscription.getName(), subscription.getApp().getName());

        sendEmailAsync(user.getEmail(), subject, content);
    }

    @PostConstruct
    public void init() {
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        if (connectionFactory instanceof CachingConnectionFactory) {
            CachingConnectionFactory ccf = (CachingConnectionFactory) connectionFactory;
            System.out.println("🔗 RabbitMQ Host: " + ccf.getHost());
            System.out.println("🔗 RabbitMQ Port: " + ccf.getPort());
            System.out.println("🔗 RabbitMQ Username: " + ccf.getUsername());
        }
    }

    private void sendEmailAsync(String to, String subject, String text) {
        try {
            EmailMessageDto message = new EmailMessageDto();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            rabbitTemplate.convertAndSend(emailQueue, message);

            log.info("📩 Сообщение поставлено в очередь для отправки на {}", to);
        } catch (Exception e) {
            log.error("Ошибка при постановке сообщения в очередь для {}: {}", to, e.getMessage());
            throw new EmailSendingException("Не удалось поставить email в очередь", e);
        }
    }
}