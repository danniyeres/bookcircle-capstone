package bookcircle.service;

import bookcircle.entity.AuditLog;
import bookcircle.repo.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Long actorUserId, String action, String entityType, Long entityId, String details) {
        AuditLog a = new AuditLog();
        a.setActorUserId(actorUserId);
        a.setAction(action);
        a.setEntityType(entityType);
        a.setEntityId(entityId);
        a.setDetails(details);
        auditLogRepository.save(a);
    }
}
