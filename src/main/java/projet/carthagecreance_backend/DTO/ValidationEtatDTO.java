package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.StatutValidationTarifs;

import java.util.Map;

/**
 * DTO pour l'état global de validation des tarifs d'un dossier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationEtatDTO {
    private Long dossierId;
    private StatutValidationTarifs statutGlobal;
    private Map<String, ValidationEtatPhaseDTO> phases;  // Clés: "CREATION", "ENQUETE", "AMIABLE", "JURIDIQUE"
    private Boolean peutGenererFacture;
}

