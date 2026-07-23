package com.driftstay.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void recordEvent(String entityType, Long entityId, String action, Long userId, String userEmail, HttpServletRequest request) {
        AuditLog audit = new AuditLog();
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setAction(action);
        audit.setUserId(userId);
        audit.setUserEmail(userEmail);
        audit.setIpAddress(request != null ? request.getRemoteAddr() : null);
        auditLogRepository.save(audit);
        log.debug("Audit: {} {} {} by user {}", action, entityType, entityId, userId);
    }

    public void recordEvent(String entityType, Long entityId, String action, Long userId, String userEmail) {
        recordEvent(entityType, entityId, action, userId, userEmail, null);
    }
}
