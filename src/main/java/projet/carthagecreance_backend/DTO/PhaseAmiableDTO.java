package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la phase amiable
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhaseAmiableDTO {
    private List<ActionAmiableTraitementDTO> actions;
}

