package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'état de validation d'une phase
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationEtatPhaseDTO {
    private String statut;  // "VALIDE", "EN_ATTENTE_VALIDATION", "REJETE"
    private Integer tarifsTotal;
    private Integer tarifsValides;
}

