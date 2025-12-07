package projet.carthagecreance_backend.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    /**
     * Date de la prédiction
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime datePrediction;
}

