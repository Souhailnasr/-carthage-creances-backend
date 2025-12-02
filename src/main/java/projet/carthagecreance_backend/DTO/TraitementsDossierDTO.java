package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO principal pour tous les traitements d'un dossier organisés par phase
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitementsDossierDTO {
    private Long dossierId;
    private PhaseCreationDTO phaseCreation;
    private PhaseEnqueteDTO phaseEnquete;  // Peut être null si pas d'enquête
    private PhaseAmiableDTO phaseAmiable;
    private PhaseJuridiqueDTO phaseJuridique;
}

