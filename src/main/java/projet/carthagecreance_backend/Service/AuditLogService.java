package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.AuditLog;
import projet.carthagecreance_backend.Entity.TypeChangementAudit;

import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des logs d'audit
 */
public interface AuditLogService {
    
    /**
     * Crée un log d'audit
     * @param dossierId ID du dossier
     * @param userId ID de l'utilisateur (peut être null)
     * @param changeType Type de changement
     * @param before État avant (Map JSON)
     * @param after État après (Map JSON)
     * @param description Description du changement
     * @return AuditLog créé
     */
    AuditLog logChangement(Long dossierId, Long userId, TypeChangementAudit changeType, 
                          Map<String, Object> before, Map<String, Object> after, String description);
    
    /**
     * Récupère les logs d'audit d'un dossier
     * @param dossierId ID du dossier
     * @return Liste des logs d'audit
     */
    List<AuditLog> getLogsByDossier(Long dossierId);
    
    /**
     * Récupère les logs d'audit d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des logs d'audit
     */
    List<AuditLog> getLogsByUser(Long userId);
}

