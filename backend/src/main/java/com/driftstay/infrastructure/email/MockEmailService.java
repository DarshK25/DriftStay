package com.driftstay.infrastructure.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class MockEmailService implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("[MOCK EMAIL] To: {} | Subject: {} | Body: {}", to, subject, body);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("[MOCK EMAIL] To: {} | Subject: {} | HTML Body length: {}", to, subject, htmlBody.length());
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
