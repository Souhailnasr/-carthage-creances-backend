package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.DossierRepository;
import projet.carthagecreance_backend.Repository.DocumentHuissierRepository;
import projet.carthagecreance_backend.Repository.RecommendationRepository;
import projet.carthagecreance_backend.Service.RecommendationService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {
    
    @Autowired
    private RecommendationRepository recommendationRepository;
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Override
    public Recommendation createRecommendationForDocument(DocumentHuissier document) {
        String ruleCode;
        String title;
        String description;
        PrioriteRecommendation priority;
        
        // Règle R1: PV_MISE_EN_DEMEURE créé
        if (document.getTypeDocument() == TypeDocumentHuissier.PV_MISE_EN_DEMEURE) {
            ruleCode = "ESCALATE_TO_ORDONNANCE";
            title = "Déposer ordonnance de paiement";
            description = "Si non payé, déposer ordonnance de paiement.";
            priority = PrioriteRecommendation.HIGH;
        }
        // Règle R3: ORDONNANCE_PAIEMENT créé
        else if (document.getTypeDocument() == TypeDocumentHuissier.ORDONNANCE_PAIEMENT) {
            ruleCode = "NOTIFY_DEBTOR";
            title = "Notifier le débiteur de l'ordonnance";
            description = "Notifier le débiteur de l'ordonnance.";
            priority = PrioriteRecommendation.HIGH;
        }
        // PV_NOTIFICATION_ORDONNANCE
        else {
            ruleCode = "WAIT_FOR_RESPONSE";
            title = "Attendre la réponse du débiteur";
            description = "Attendre la réponse du débiteur après notification de l'ordonnance.";
            priority = PrioriteRecommendation.MEDIUM;
        }
        
        return createRecommendation(document.getDossierId(), ruleCode, title, description, priority);
    }
    
    @Override
    public Recommendation createRecommendationForExpiredDocument(DocumentHuissier document) {
        String ruleCode;
        String title;
        String description;
        PrioriteRecommendation priority = PrioriteRecommendation.HIGH;
        
        // Règle R2: PV_MISE_EN_DEMEURE expiré
        if (document.getTypeDocument() == TypeDocumentHuissier.PV_MISE_EN_DEMEURE) {
            ruleCode = "ESCALATE_TO_ORDONNANCE";
            title = "Déposer ordonnance de paiement";
            description = "Le délai légal du PV de mise en demeure a expiré. Déposer une ordonnance de paiement.";
        }
        // Règle R4: ORDONNANCE_PAIEMENT expiré
        else if (document.getTypeDocument() == TypeDocumentHuissier.ORDONNANCE_PAIEMENT) {
            ruleCode = "INITIATE_EXECUTION";
            title = "Initier action d'exécution (saisie conservatoire)";
            description = "Le délai légal de l'ordonnance de paiement a expiré. Initier une action d'exécution (saisie conservatoire - ACLA_TA7AFOUDHIA).";
        }
        // PV_NOTIFICATION_ORDONNANCE expiré
        else {
            ruleCode = "INITIATE_EXECUTION";
            title = "Initier action d'exécution";
            description = "Le délai légal de notification de l'ordonnance a expiré. Initier une action d'exécution.";
        }
        
        return createRecommendation(document.getDossierId(), ruleCode, title, description, priority);
    }
    
    @Override
    public Recommendation createRecommendationForAction(ActionHuissier action, Dossier dossier) {
        // Règle R5: Si montantRestant == 0, créer recommandation DONE
        if (action.getMontantRestant() != null && 
            action.getMontantRestant().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return createRecommendation(
                action.getDossierId(),
                "DONE",
                "Dossier totalement recouvré",
                "Le dossier a été totalement recouvré. Vous pouvez le clôturer.",
                PrioriteRecommendation.MEDIUM
            );
        }
        
        // Règle R6: Si montantRestant > 50% de montantTotal, recommander avocat
        if (dossier.getMontantTotal() != null && dossier.getMontantRestant() != null) {
            double montantTotal = dossier.getMontantTotal();
            double montantRestant = dossier.getMontantRestant();
            
            if (montantTotal > 0) {
                double percentage = (montantRestant / montantTotal) * 100;
                if (percentage > 50) {
                    return createRecommendation(
                        action.getDossierId(),
                        "ASSIGN_AVOCAT",
                        "Assigner un avocat",
                        String.format("Plus de 50%% du montant reste à recouvrer (%.2f%%). Considérer l'assignation d'un avocat.", percentage),
                        PrioriteRecommendation.MEDIUM
                    );
                }
            }
        }
        
        // Si montantRestant > 0 mais < 50%, suggérer de continuer l'exécution
        if (action.getMontantRestant() != null && 
            action.getMontantRestant().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return createRecommendation(
                action.getDossierId(),
                "CONTINUE_EXECUTION",
                "Continuer l'exécution",
                "Montant partiellement recouvré. Considérer d'autres actions d'exécution si nécessaire.",
                PrioriteRecommendation.LOW
            );
        }
        
        return null; // Aucune recommandation nécessaire
    }
    
    @Override
    public List<Recommendation> evaluateAndCreateRecommendations(Dossier dossier) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Règle R7: Si dossier inactif > 90 jours et montantRestant > 0
        if (dossier.getDateCreation() != null && dossier.getMontantRestant() != null && 
            dossier.getMontantRestant() > 0) {
            
            Instant creationDate = dossier.getDateCreation().toInstant();
            long daysSinceCreation = ChronoUnit.DAYS.between(creationDate, Instant.now());
            
            if (daysSinceCreation > 90) {
                Recommendation rec = createRecommendation(
                    dossier.getId(),
                    "ESCALATE_TO_DIRECTOR",
                    "Escalader au directeur",
                    String.format("Le dossier est inactif depuis %d jours. Considérer l'escalade au directeur.", daysSinceCreation),
                    PrioriteRecommendation.HIGH
                );
                recommendations.add(rec);
            }
        }
        
        // Vérifier si des documents sont expirés sans action suivante
        List<DocumentHuissier> expiredDocuments = documentHuissierRepository.findByDossierIdAndStatus(
            dossier.getId(), 
            StatutDocumentHuissier.EXPIRED
        );
        
        for (DocumentHuissier expiredDoc : expiredDocuments) {
            // Vérifier si une recommandation existe déjà pour ce document
            List<Recommendation> existingRecs = recommendationRepository.findByDossierIdAndAcknowledged(
                dossier.getId(), 
                false
            );
            
            boolean hasRecommendation = existingRecs.stream()
                .anyMatch(r -> r.getRuleCode().equals(getRuleCodeForExpiredDocument(expiredDoc)));
            
            if (!hasRecommendation) {
                Recommendation rec = createRecommendationForExpiredDocument(expiredDoc);
                if (rec != null) {
                    recommendations.add(rec);
                }
            }
        }
        
        return recommendations;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getRecommendationsByDossier(Long dossierId) {
        return recommendationRepository.findByDossierId(dossierId);
    }
    
    @Override
    public void acknowledgeRecommendation(Long recommendationId, Long userId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recommandation non trouvée avec l'ID: " + recommendationId));
        
        recommendation.setAcknowledged(true);
        recommendation.setAcknowledgedAt(Instant.now());
        recommendation.setAcknowledgedBy(userId);
        
        recommendationRepository.save(recommendation);
    }
    
    private Recommendation createRecommendation(Long dossierId, String ruleCode, 
                                               String title, String description, 
                                               PrioriteRecommendation priority) {
        Recommendation recommendation = Recommendation.builder()
                .dossierId(dossierId)
                .ruleCode(ruleCode)
                .title(title)
                .description(description)
                .priority(priority)
                .createdAt(Instant.now())
                .acknowledged(false)
                .build();
        
        return recommendationRepository.save(recommendation);
    }
    
    private String getRuleCodeForExpiredDocument(DocumentHuissier document) {
        if (document.getTypeDocument() == TypeDocumentHuissier.PV_MISE_EN_DEMEURE) {
            return "ESCALATE_TO_ORDONNANCE";
        } else {
            return "INITIATE_EXECUTION";
        }
    }
}

