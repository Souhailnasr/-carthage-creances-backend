package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.DocumentHuissierDTO;
import projet.carthagecreance_backend.Entity.DocumentHuissier;

import java.util.List;

/**
 * Service pour la gestion des documents huissier
 */
public interface DocumentHuissierService {

    /**
     * Crée un document huissier (Phase 1 ou 2)
     * @param dto DTO du document
     * @return Document créé
     */
    DocumentHuissier createDocument(DocumentHuissierDTO dto);

    /**
     * Récupère un document par son ID
     * @param id ID du document
     * @return Document trouvé
     */
    DocumentHuissier getDocumentById(Long id);

    /**
     * Récupère tous les documents d'un dossier
     * @param dossierId ID du dossier
     * @return Liste des documents
     */
    List<DocumentHuissier> getDocumentsByDossier(Long dossierId);

    /**
     * Marque un document comme expiré
     * @param documentId ID du document
     * @return Document mis à jour
     */
    DocumentHuissier markAsExpired(Long documentId);

    /**
     * Marque un document comme complété
     * @param documentId ID du document
     * @return Document mis à jour
     * @throws RuntimeException si le document est EXPIRED ou déjà COMPLETED
     */
    DocumentHuissier markAsCompleted(Long documentId);
}