package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.*;
import projet.carthagecreance_backend.Entity.StatutTarif;
import projet.carthagecreance_backend.Entity.TarifDossier;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des tarifs de dossier
 */
public interface TarifDossierService {
    
    /**
     * Récupère tous les traitements d'un dossier organisés par phase
     */
    TraitementsDossierDTO getTraitementsDossier(Long dossierId);
    
    /**
     * Crée un nouveau tarif pour un dossier
     */
    TarifDossierDTO createTarif(Long dossierId, TarifDossierRequest request);
    
    /**
     * Valide un tarif
     */
    TarifDossierDTO validerTarif(Long tarifId, String commentaire);
    
    /**
     * Rejette un tarif
     */
    TarifDossierDTO rejeterTarif(Long tarifId, String commentaire);
    
    /**
     * Récupère l'état de validation des tarifs d'un dossier
     */
    ValidationEtatDTO getValidationEtat(Long dossierId);
    
    /**
     * Récupère le détail de la facture avec les frais d'enquête inclus
     */
    DetailFactureDTO getDetailFacture(Long dossierId);
    
    /**
     * Génère la facture une fois tous les tarifs validés
     */
    projet.carthagecreance_backend.DTO.FactureDTO genererFacture(Long dossierId);
    
    /**
     * Récupère un tarif par ID
     */
    Optional<TarifDossier> getTarifById(Long tarifId);
    
    /**
     * Récupère tous les tarifs d'un dossier
     */
    List<TarifDossierDTO> getTarifsByDossier(Long dossierId);
}

