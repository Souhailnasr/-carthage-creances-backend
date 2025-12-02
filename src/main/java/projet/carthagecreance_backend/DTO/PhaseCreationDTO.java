package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la phase de création
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseCreationDTO {
    private List<TraitementDTO> traitements;
}

