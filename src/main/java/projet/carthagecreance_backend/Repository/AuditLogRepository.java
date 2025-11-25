package projet.carthagecreance_backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projet.carthagecreance_backend.Entity.AuditLog;
import projet.carthagecreance_backend.Entity.TypeChangementAudit;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByDossierId(Long dossierId);
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByChangeType(TypeChangementAudit changeType);
    
    List<AuditLog> findByDossierIdOrderByTimestampDesc(Long dossierId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.dossierId = :dossierId AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findByDossierIdSince(@Param("dossierId") Long dossierId, @Param("since") Instant since);
}

