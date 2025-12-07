package projet.carthagecreance_backend.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import projet.carthagecreance_backend.Event.DossierDataChangedEvent;
import projet.carthagecreance_backend.Service.IaPredictionRecalculationService;

/**
 * Listener pour les événements de changement de données de dossier
 * Déclenche automatiquement le recalcul de la prédiction IA
 */
@Component
public class DossierDataChangedListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DossierDataChangedListener.class);
    
    @Autowired
    private IaPredictionRecalculationService iaPredictionRecalculationService;
    
    /**
     * Écoute les événements de changement de données de dossier
     * et déclenche le recalcul automatique de la prédiction IA
     * 
     * @param event L'événement de changement de données
     */
    @EventListener
    @Async
    public void handleDossierDataChanged(DossierDataChangedEvent event) {
        Long dossierId = event.getDossierId();
        String changeType = event.getChangeType();
        
        logger.debug("Événement de changement détecté pour le dossier {}: type={}", dossierId, changeType);
        
        try {
            // Recalculer la prédiction IA de manière asynchrone
            boolean success = iaPredictionRecalculationService.recalculatePrediction(dossierId);
            
            if (success) {
                logger.debug("Recalcul automatique de la prédiction IA réussi pour le dossier {} après changement de type {}", 
                    dossierId, changeType);
            } else {
                logger.debug("Recalcul automatique de la prédiction IA ignoré pour le dossier {} après changement de type {}", 
                    dossierId, changeType);
            }
            
        } catch (Exception e) {
            logger.warn("Erreur lors du recalcul automatique de la prédiction IA pour le dossier {} après changement de type {}: {}", 
                dossierId, changeType, e.getMessage());
        }
    }
}

