package projet.carthagecreance_backend.Service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.DTO.IaPredictionResult;
import projet.carthagecreance_backend.Entity.*;
import projet.carthagecreance_backend.Repository.*;
import projet.carthagecreance_backend.Service.IaPredictionRecalculationService;
import projet.carthagecreance_backend.Service.IaPredictionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implémentation du service de recalcul automatique de la prédiction IA
 */
@Service
public class IaPredictionRecalculationServiceImpl implements IaPredictionRecalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IaPredictionRecalculationServiceImpl.class);
    
    @Autowired
    private DossierRepository dossierRepository;
    
    @Autowired
    private EnquetteRepository enquetteRepository;
    
    @Autowired
    private ActionRepository actionRepository;
    
    @Autowired
    private AudienceRepository audienceRepository;
    
    @Autowired
    private ActionHuissierRepository actionHuissierRepository;
    
    @Autowired
    private DocumentHuissierRepository documentHuissierRepository;
    
    @Autowired
    private IaPredictionService iaPredictionService;
    
    @Autowired
    private IaFeatureBuilderService iaFeatureBuilderService;
    
    @Override
    @Transactional
    public boolean recalculatePrediction(Long dossierId) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("Début du recalcul automatique de la prédiction IA pour le dossier {}", dossierId);
            
            // 1. Vérifier que le dossier existe
            Dossier dossier = dossierRepository.findById(dossierId)
                    .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
            
            // 2. Vérifier que le dossier a suffisamment de données pour une prédiction fiable
            if (dossier.getMontantCreance() == null || dossier.getMontantCreance() <= 0) {
                logger.debug("Dossier {} n'a pas de montant de créance valide, recalcul ignoré", dossierId);
                return false;
            }
            
            // 3. Récupérer toutes les données associées récentes
            Optional<Enquette> enqueteOpt = enquetteRepository.findByDossierId(dossierId);
            List<Action> actions = actionRepository.findByDossierId(dossierId);
            List<Audience> audiences = audienceRepository.findByDossierId(dossierId);
            List<ActionHuissier> actionsHuissier = actionHuissierRepository.findByDossierId(dossierId);
            List<DocumentHuissier> documentsHuissier = documentHuissierRepository.findByDossierId(dossierId);
            
            // 4. Construire les features à partir des données réelles
            Map<String, Object> features = iaFeatureBuilderService.buildFeaturesFromRealData(
                dossier,
                enqueteOpt.orElse(null),
                actions,
                audiences,
                actionsHuissier,
                documentsHuissier
            );
            
            // 5. Prédire avec l'IA
            IaPredictionResult prediction = iaPredictionService.predictRisk(features);
            
            // 6. Mettre à jour le dossier avec les résultats de la prédiction
            LocalDateTime now = LocalDateTime.now();
            dossier.setEtatPrediction(EtatDossier.valueOf(prediction.getEtatFinal()));
            dossier.setRiskScore(prediction.getRiskScore());
            dossier.setRiskLevel(prediction.getRiskLevel());
            dossier.setDatePrediction(now);
            
            // 7. Sauvegarder le dossier mis à jour
            dossierRepository.save(dossier);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Recalcul automatique de la prédiction IA réussi pour le dossier {}: etatFinal={}, riskScore={}, riskLevel={}, durée={}ms", 
                dossierId, prediction.getEtatFinal(), prediction.getRiskScore(), prediction.getRiskLevel(), duration);
            
            return true;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.warn("Erreur lors du recalcul automatique de la prédiction IA pour le dossier {} (durée: {}ms): {}. Le recalcul sera ignoré.", 
                dossierId, duration, e.getMessage());
            return false;
        }
    }
}

