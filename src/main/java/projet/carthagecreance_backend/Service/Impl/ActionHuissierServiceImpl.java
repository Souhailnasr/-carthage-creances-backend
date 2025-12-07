package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.ActionHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.ActionHuissierRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.ActionHuissierService;
import projet.carthagecreance_backend.Service.DossierMontantService;
import projet.carthagecreance_backend.Service.NotificationHuissierService;
import projet.carthagecreance_backend.Service.AutomaticNotificationService;
import projet.carthagecreance_backend.Service.RecommendationService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ActionHuissierServiceImpl implements ActionHuissierService {
    
    @Autowired
    private ActionHuissierRepository actionHuissierRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private DossierMontantService dossierMontantService;
    
    @Autowired
    private NotificationHuissierService notificationHuissierService; // Gardé pour compatibilité
    
    @Autowired
    private AutomaticNotificationService automaticNotificationService; // Nouveau système unifié
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private projet.carthagecreance_backend.Service.StatistiqueService statistiqueService;
    
    @Override
    public ActionHuissier createAction(ActionHuissierDTO dto) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dto.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dto.getDossierId()));
        
        // Valider les montants
        if (dto.getMontantRecouvre() != null && dto.getMontantRecouvre().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant recouvré ne peut pas être négatif");
        }
        
        // Récupérer les valeurs actuelles du dossier (avant mise à jour)
        BigDecimal montantRestantAvant = dossier.getMontantRestant() != null ? 
            BigDecimal.valueOf(dossier.getMontantRestant()) : BigDecimal.ZERO;
        EtatDossier etatDossierAvant = dossier.getEtatDossier();
        
        // Créer l'action huissier d'abord
        ActionHuissier action = ActionHuissier.builder()
                .dossierId(dto.getDossierId())
                .typeAction(dto.getTypeAction())
                .montantRecouvre(dto.getMontantRecouvre())
                .montantRestant(montantRestantAvant)
                .etatDossier(etatDossierAvant)
                .dateAction(dto.getDateAction() != null ? dto.getDateAction() : Instant.now())
                .pieceJointeUrl(dto.getPieceJointeUrl())
                .huissierName(dto.getHuissierName())
                .build();
        
        ActionHuissier saved = actionHuissierRepository.save(action);
        
        // ✅ Mettre à jour le montant recouvré phase juridique APRÈS création de l'action (pour avoir l'ID)
        if (dto.getMontantRecouvre() != null && saved.getId() != null) {
            ModeMiseAJour updateMode = dto.getUpdateMode() != null ? dto.getUpdateMode() : ModeMiseAJour.ADD;
            dossier = dossierMontantService.updateMontantRecouvrePhaseJuridique(
                dto.getDossierId(),
                dto.getMontantRecouvre(),
                updateMode,
                saved.getId(), // ID de l'action huissier créée
                dto.getUtilisateurId(), // Passé depuis le contrôleur
                HistoriqueRecouvrement.TypeActionRecouvrement.ACTION_HUISSIER,
                "Recouvrement suite à action huissier: " + dto.getTypeAction()
            );
            
            // Mettre à jour l'action avec les nouvelles valeurs
            saved.setMontantRestant(dossier.getMontantRestant() != null ? 
                BigDecimal.valueOf(dossier.getMontantRestant()) : BigDecimal.ZERO);
            saved.setEtatDossier(dossier.getEtatDossier());
            saved = actionHuissierRepository.save(saved);
        }
        
        // Créer une notification - Ancien système
        try {
            notificationHuissierService.notifyActionPerformed(saved, dossier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification (ancien système): " + e.getMessage());
        }
        
        // Créer une notification via le système unifié
        try {
            automaticNotificationService.notifierActionHuissierEffectuee(saved, dossier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification unifiée: " + e.getMessage());
        }
        
        // Créer une recommandation si nécessaire
        try {
            recommendationService.createRecommendationForAction(saved, dossier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la recommandation: " + e.getMessage());
        }
        
        // Recalcul automatique des statistiques (asynchrone)
        try {
            statistiqueService.recalculerStatistiquesAsync();
        } catch (Exception e) {
            System.err.println("Erreur lors du recalcul automatique des statistiques après création d'action huissier: " + e.getMessage());
        }
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ActionHuissier getActionById(Long id) {
        return actionHuissierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Action non trouvée avec l'ID: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ActionHuissier> getActionsByDossier(Long dossierId) {
        return actionHuissierRepository.findByDossierId(dossierId);
    }
}

