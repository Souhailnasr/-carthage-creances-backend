package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la phase juridique
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseJuridiqueDTO {
    private List<DocumentHuissierTraitementDTO> documentsHuissier;
    private List<ActionHuissierTraitementDTO> actionsHuissier;
    private List<AudienceTraitementDTO> audiences;
}

