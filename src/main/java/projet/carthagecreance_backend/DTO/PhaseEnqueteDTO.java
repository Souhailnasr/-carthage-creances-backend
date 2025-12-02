package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la phase d'enquête
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseEnqueteDTO {
    private TraitementDTO enquetePrecontentieuse;  // Obligatoire
    private List<TraitementPossibleDTO> traitementsPossibles;  // Optionnels (Expertise, Déplacement, Autres)
}

