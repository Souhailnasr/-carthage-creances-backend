package projet.carthagecreance_backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration pour activer le support asynchrone dans Spring
 * Permet l'exécution asynchrone des listeners d'événements
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Configuration par défaut pour l'exécution asynchrone
}

