package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le résultat de la prédiction IA
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IaPredictionResult {
    /**
     * État final prédit : RECOVERED_TOTAL, RECOVERED_PARTIAL, NOT_RECOVERED
     */
    private String etatFinal;
    
    /**
     * Score de risque (0-100)
     */
    private Double riskScore;
    
    /**
     * Niveau de risque : "Faible", "Moyen", "Élevé"
     */
    private String riskLevel;
}

