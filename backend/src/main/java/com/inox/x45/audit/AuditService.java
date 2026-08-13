package com.inox.x45.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inox.x45.domain.AuditLog;
import com.inox.x45.repository.AuditLogRepository;
import com.inox.x45.security.CurrentUserResolver;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Every mutating action writes an audit log entry (Section 4, Section 6.1
 * step 6). Call this from the same transaction as the mutation it describes.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserResolver currentUserResolver;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, CurrentUserResolver currentUserResolver, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserResolver = currentUserResolver;
        this.objectMapper = objectMapper;
    }

    public void record(Authentication actor, String action, String entityName, Long entityId, Object before, Object after) {
        AuditLog log = new AuditLog();
        log.setActorEmail(currentUserResolver.resolve(actor).email());
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setBeforeJson(toJsonOrNull(before));
        log.setAfterJson(toJsonOrNull(after));
        log.setOccurredAt(Instant.now());
        auditLogRepository.save(log);
    }

    private String toJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"error\":\"failed to serialize: " + e.getMessage() + "\"}";
        }
    }
}
