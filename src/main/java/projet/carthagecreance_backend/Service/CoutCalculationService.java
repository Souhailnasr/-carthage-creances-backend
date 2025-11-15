package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.Entity.Finance;

/**
 * Service dédié pour les calculs automatiques des coûts
 * Centralise toute la logique de calcul des coûts pour Finance
 */
public interface CoutCalculationService {
    
    /**
     * Calcule le coût total des actions de recouvrement amiable pour un dossier
     * @param dossierId ID du dossier
     * @return Coût total des actions amiable
     */
    Double calculerCoutActionsAmiable(Long dossierId);
    
    /**
     * Calcule le coût total des actions de recouvrement juridique pour un dossier
     * @param dossierId ID du dossier
     * @return Coût total des actions juridique
     */
    Double calculerCoutActionsJuridique(Long dossierId);
    
    /**
     * Calcule la durée de gestion en mois entre la création et la clôture (ou aujourd'hui)
     * @param dossierId ID du dossier
     * @return Nombre de mois (arrondi supérieur)
     */
    Integer calculerDureeGestion(Long dossierId);
    
    /**
     * Calcule le coût total de gestion (fraisGestionDossier * dureeGestionMois)
     * @param dossierId ID du dossier
     * @return Coût total de gestion
     */
    Double calculerCoutGestion(Long dossierId);
    
    /**
     * Recalcule tous les coûts d'un dossier et met à jour la Finance
     * @param dossierId ID du dossier
     * @return Finance mise à jour
     */
    Finance recalculerTousLesCouts(Long dossierId);
    
    /**
     * Synchronise toutes les actions d'un dossier avec sa Finance
     * Recalcule coutActionsAmiable, coutActionsJuridique, nombreActionsAmiable, nombreActionsJuridique
     * @param dossierId ID du dossier
     */
    void synchroniserActionsAvecFinance(Long dossierId);
}

