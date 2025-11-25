package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.ActionHuissierDTO;
import projet.carthagecreance_backend.Entity.ActionHuissier;

import java.util.List;

/**
 * Service pour la gestion des actions huissier (Phase 3 - Exécution)
 */
public interface ActionHuissierService {
    
    /**
     * Crée une action huissier et met à jour le dossier
     * @param dto DTO de l'action
     * @return Action créée
     */
    ActionHuissier createAction(ActionHuissierDTO dto);
    
    /**
     * Récupère une action par son ID
     * @param id ID de l'action
     * @return Action trouvée
     */
    ActionHuissier getActionById(Long id);
    
    /**
     * Récupère toutes les actions d'un dossier
     * @param dossierId ID du dossier
     * @return Liste des actions
     */
    List<ActionHuissier> getActionsByDossier(Long dossierId);
}

