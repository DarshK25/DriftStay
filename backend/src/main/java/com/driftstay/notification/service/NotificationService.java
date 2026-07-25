package com.driftstay.notification.service;

import com.driftstay.common.enums.NotificationChannel;
import com.driftstay.common.enums.NotificationStatus;
import com.driftstay.notification.entity.Notification;
import com.driftstay.notification.entity.NotificationLog;
import com.driftstay.notification.entity.NotificationTemplate;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EntityManager entityManager;

    @Transactional
    public Notification sendNotification(Long userId, String templateName,
                                         NotificationChannel channel, String recipient,
                                         Map<String, String> variables) {
        // Find template
        NotificationTemplate template = entityManager.createQuery(
                        "SELECT t FROM NotificationTemplate t WHERE t.name = :name AND t.channel = :channel",
                        NotificationTemplate.class)
                .setParameter("name", templateName)
                .setParameter("channel", channel)
                .getSingleResult();

        // Render body with variables
        String renderedBody = renderTemplate(template.getBody(), variables);
        String renderedSubject = template.getSubject() != null
                ? renderTemplate(template.getSubject(), variables)
                : null;

        // Create notification
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTemplateId(template.getId());
        notification.setChannel(channel);
        notification.setRecipient(recipient);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRenderedBody(renderedBody);
        notification.setScheduledAt(LocalDateTime.now());

        entityManager.persist(notification);

        // TODO: Implement actual sending (email/SMS/push) via provider
        // For now, mark as sent immediately (mock)
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        NotificationLog logEntry = new NotificationLog();
        logEntry.setNotificationId(notification.getId());
        logEntry.setProvider(channel.name().toLowerCase() + "_mock");
        logEntry.setProviderResponse("{\"status\": \"sent\", \"mode\": \"mock\"}");
        logEntry.setStatus(com.driftstay.common.enums.NotificationLogStatus.SENT);
        entityManager.persist(logEntry);

        log.info("Notification sent: userId={}, template={}, channel={}", userId, templateName, channel);
        return notification;
    }

    private String renderTemplate(String template, Map<String, String> variables) {
        String result = template;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return result;
    }
}
