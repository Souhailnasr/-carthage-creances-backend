package projet.carthagecreance_backend.Service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.AuditLog;
import projet.carthagecreance_backend.Entity.TypeChangementAudit;
import projet.carthagecreance_backend.Repository.AuditLogRepository;
import projet.carthagecreance_backend.Service.AuditLogService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public AuditLog logChangement(Long dossierId, Long userId, TypeChangementAudit changeType, 
                                 Map<String, Object> before, Map<String, Object> after, String description) {
        try {
            String beforeJson = objectMapper.writeValueAsString(before);
            String afterJson = objectMapper.writeValueAsString(after);
            
            AuditLog auditLog = AuditLog.builder()
                    .dossierId(dossierId)
                    .userId(userId)
                    .changeType(changeType)
                    .before(beforeJson)
                    .after(afterJson)
                    .description(description)
                    .timestamp(Instant.now())
                    .build();
            
            return auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la sérialisation JSON pour l'audit log", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByDossier(Long dossierId) {
        return auditLogRepository.findByDossierIdOrderByTimestampDesc(dossierId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
}

