package projet.carthagecreance_backend.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Repository.PasswordResetTokenRepository;

import java.time.LocalDateTime;

/**
 * Scheduler pour nettoyer automatiquement les tokens expirés et anciens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetScheduler {
    
    private final PasswordResetTokenRepository tokenRepository;
    
    /**
     * Marque les tokens expirés toutes les heures
     * @Transactional requis car expireTokens() utilise @Modifying
     */
    @Scheduled(fixedRate = 3600000) // 1 heure en millisecondes
    @Transactional
    public void expireTokens() {
        log.info("Marquage des tokens expirés...");
        try {
            tokenRepository.expireTokens(LocalDateTime.now());
            log.info("Tokens expirés marqués avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du marquage des tokens expirés: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Supprime les tokens anciens (expirés ou utilisés de plus de 7 jours) tous les jours à 2h du matin
     * @Transactional requis car deleteOldTokens() utilise @Modifying
     */
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
    @Transactional
    public void deleteOldTokens() {
        log.info("Suppression des tokens anciens...");
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            tokenRepository.deleteOldTokens(cutoffDate);
            log.info("Tokens anciens supprimés avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des tokens anciens: {}", e.getMessage(), e);
        }
    }
}

