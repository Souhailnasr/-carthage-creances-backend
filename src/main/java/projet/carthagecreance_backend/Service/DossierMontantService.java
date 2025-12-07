package projet.carthagecreance_backend.Service;

import projet.carthagecreance_backend.DTO.MontantDossierDTO;
import projet.carthagecreance_backend.Entity.Dossier;
import projet.carthagecreance_backend.Entity.ModeMiseAJour;

/**
 * Service pour la gestion des montants des dossiers
 * Gère la mise à jour des montants et le calcul automatique de l'état
 */
public interface DossierMontantService {
    
    /**
     * Met à jour les montants d'un dossier
     * @param dossierId ID du dossier
     * @param dto DTO contenant les montants et le mode de mise à jour
     * @return Dossier mis à jour
     */
    Dossier updateMontants(Long dossierId, MontantDossierDTO dto);
    
    /**
     * Met à jour le montant recouvré depuis une action amiable
     * @param dossierId ID du dossier
     * @param montantRecouvre Montant recouvré
     * @param updateMode Mode de mise à jour (ADD ou SET)
     * @return Dossier mis à jour
     */
    Dossier updateMontantRecouvreAmiable(Long dossierId, java.math.BigDecimal montantRecouvre, ModeMiseAJour updateMode);
    
    /**
     * Met à jour le montant recouvré depuis une action amiable (avec traçabilité par phase)
     * @param dossierId ID du dossier
     * @param montantRecouvre Montant recouvré
     * @param updateMode Mode de mise à jour (ADD ou SET)
     * @param actionId ID de l'action amiable (optionnel)
     * @param utilisateurId ID de l'utilisateur qui enregistre
     * @param commentaire Commentaire optionnel
     * @return Dossier mis à jour
     */
    Dossier updateMontantRecouvrePhaseAmiable(
            Long dossierId, 
            java.math.BigDecimal montantRecouvre, 
            ModeMiseAJour updateMode,
            Long actionId,
            Long utilisateurId,
            String commentaire);
    
    /**
     * Met à jour le montant recouvré depuis une action juridique (huissier ou finalisation)
     * @param dossierId ID du dossier
     * @param montantRecouvre Montant recouvré
     * @param updateMode Mode de mise à jour (ADD ou SET)
     * @param actionId ID de l'action huissier ou null pour finalisation
     * @param utilisateurId ID de l'utilisateur qui enregistre
     * @param typeAction Type d'action (ACTION_HUISSIER ou FINALISATION_JURIDIQUE)
     * @param commentaire Commentaire optionnel
     * @return Dossier mis à jour
     */
    Dossier updateMontantRecouvrePhaseJuridique(
            Long dossierId,
            java.math.BigDecimal montantRecouvre,
            ModeMiseAJour updateMode,
            Long actionId,
            Long utilisateurId,
            projet.carthagecreance_backend.Entity.HistoriqueRecouvrement.TypeActionRecouvrement typeAction,
            String commentaire);
    
    /**
     * Recalcule le montant restant et l'état du dossier
     * @param dossier Dossier à mettre à jour
     * @return Dossier mis à jour
     */
    Dossier recalculerMontantRestantEtEtat(Dossier dossier);
}

