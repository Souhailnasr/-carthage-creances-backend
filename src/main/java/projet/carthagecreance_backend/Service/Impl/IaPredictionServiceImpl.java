package projet.carthagecreance_backend.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.DTO.IaPredictionResult;
import projet.carthagecreance_backend.Service.IaPredictionService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Implémentation du service de prédiction IA
 * Exécute le script Python predict.py avec les données réelles
 */
@Service
public class IaPredictionServiceImpl implements IaPredictionService {
    
    private static final Logger logger = LoggerFactory.getLogger(IaPredictionServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public IaPredictionResult predictRisk(Map<String, Object> donneesReelles) {
        Path tempFile = null;
        Process process = null;
        
        try {
            // 1. Sauvegarder les données réelles dans un fichier temporaire JSON
            tempFile = Files.createTempFile("ia_input_", ".json");
            Files.write(tempFile, objectMapper.writeValueAsBytes(donneesReelles));
            logger.debug("Fichier temporaire créé: {}", tempFile.toString());
            
            // 2. Obtenir le chemin du script Python
            ClassPathResource scriptResource = new ClassPathResource("ia/predict.py");
            File scriptFile = scriptResource.getFile();
            String scriptPath = scriptFile.getAbsolutePath();
            logger.debug("Script Python: {}", scriptPath);
            
            // 3. Vérifier que Python est disponible
            String pythonCommand = findPythonCommand();
            if (pythonCommand == null) {
                logger.error("Python n'est pas trouvé dans le PATH");
                return createFallbackResult("Python non disponible");
            }
            
            // 4. Exécuter le script Python
            logger.info("Exécution de la prédiction IA pour le dossier...");
            process = new ProcessBuilder(
                pythonCommand,
                scriptPath,
                tempFile.toString()
            )
            .redirectErrorStream(true)
            .start();
            
            // 5. Lire la sortie du script
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                logger.error("Le script Python a retourné le code d'erreur: {}", exitCode);
                logger.error("Sortie du script: {}", output);
                return createFallbackResult("Erreur lors de l'exécution du script Python");
            }
            
            // 6. Parser la réponse JSON
            logger.debug("Réponse du script Python: {}", output);
            IaPredictionResult result = objectMapper.readValue(output.trim(), IaPredictionResult.class);
            logger.info("Prédiction IA réussie: etatFinal={}, riskScore={}, riskLevel={}", 
                result.getEtatFinal(), result.getRiskScore(), result.getRiskLevel());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la prédiction IA: {}", e.getMessage(), e);
            return createFallbackResult("Erreur: " + e.getMessage());
        } finally {
            // 7. Nettoyer les ressources
            try {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile);
                }
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
            } catch (Exception e) {
                logger.warn("Erreur lors du nettoyage des ressources: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Trouve la commande Python disponible (python, python3, py)
     */
    private String findPythonCommand() {
        String[] commands = {"python3", "python", "py"};
        
        for (String cmd : commands) {
            try {
                Process p = new ProcessBuilder(cmd, "--version").start();
                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    logger.debug("Python trouvé: {}", cmd);
                    return cmd;
                }
            } catch (Exception e) {
                // Continuer avec la commande suivante
            }
        }
        
        return null;
    }
    
    /**
     * Crée un résultat de fallback en cas d'erreur
     */
    private IaPredictionResult createFallbackResult(String reason) {
        logger.warn("Utilisation du résultat de fallback: {}", reason);
        return IaPredictionResult.builder()
                .etatFinal("NOT_RECOVERED")
                .riskScore(100.0)
                .riskLevel("Élevé")
                .build();
    }
}

