package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.ActionHuissierDTO;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.ActionHuissierRepository;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Service.ActionHuissierService;
import projet.carthagecreance_backend.Service.AuditLogService;
import projet.carthagecreance_backend.Service.DossierMontantService;
import projet.carthagecreance_backend.Service.NotificationHuissierService;
import projet.carthagecreance_backend.Service.RecommendationService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private AuditLogService auditLogService;
    
    @Autowired
    private NotificationHuissierService notificationHuissierService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Override
    public ActionHuissier createAction(ActionHuissierDTO dto) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dto.getDossierId())
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dto.getDossierId()));
        
        // Valider les montants
        if (dto.getMontantRecouvre() != null && dto.getMontantRecouvre().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant recouvré ne peut pas être négatif");
        }
        
        // Mettre à jour le montant recouvré du dossier si fourni
        if (dto.getMontantRecouvre() != null) {
            ModeMiseAJour updateMode = dto.getUpdateMode() != null ? dto.getUpdateMode() : ModeMiseAJour.ADD;
            dossier = dossierMontantService.updateMontantRecouvreAmiable(
                dto.getDossierId(), 
                dto.getMontantRecouvre(), 
                updateMode
            );
        }
        
        // Récupérer les valeurs mises à jour du dossier
        BigDecimal montantRestant = dossier.getMontantRestant() != null ? 
            BigDecimal.valueOf(dossier.getMontantRestant()) : BigDecimal.ZERO;
        EtatDossier etatDossier = dossier.getEtatDossier();
        
        // Créer l'action huissier
        ActionHuissier action = ActionHuissier.builder()
                .dossierId(dto.getDossierId())
                .typeAction(dto.getTypeAction())
                .montantRecouvre(dto.getMontantRecouvre())
                .montantRestant(montantRestant)
                .etatDossier(etatDossier)
                .dateAction(dto.getDateAction() != null ? dto.getDateAction() : Instant.now())
                .pieceJointeUrl(dto.getPieceJointeUrl())
                .huissierName(dto.getHuissierName())
                .build();
        
        ActionHuissier saved = actionHuissierRepository.save(action);
        
        // Créer un audit log
        try {
            Map<String, Object> after = new HashMap<>();
            after.put("actionId", saved.getId());
            after.put("typeAction", saved.getTypeAction());
            after.put("montantRecouvre", saved.getMontantRecouvre());
            after.put("montantRestant", saved.getMontantRestant());
            after.put("etatDossier", saved.getEtatDossier());
            
            auditLogService.logChangement(
                dto.getDossierId(),
                null,
                TypeChangementAudit.ACTION_CREATE,
                new HashMap<>(),
                after,
                "Création de l'action huissier: " + saved.getTypeAction()
            );
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'audit log: " + e.getMessage());
        }
        
        // Créer une notification
        try {
            notificationHuissierService.notifyActionPerformed(saved, dossier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification: " + e.getMessage());
        }
        
        // Créer une recommandation si nécessaire
        try {
            recommendationService.createRecommendationForAction(saved, dossier);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la recommandation: " + e.getMessage());
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

