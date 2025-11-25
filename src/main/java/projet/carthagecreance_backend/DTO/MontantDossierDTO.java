package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.ModeMiseAJour;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MontantDossierDTO {
    private BigDecimal montantTotal;
    private BigDecimal montantRecouvre;
    private ModeMiseAJour updateMode; // ADD ou SET
}

