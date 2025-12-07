package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO pour la finalisation d'un dossier (juridique ou amiable)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalisationDossierDTO {
    /**
     * État final du dossier
     * RECOUVREMENT_TOTAL : Montant totalement recouvré
     * RECOUVREMENT_PARTIEL : Montant partiellement recouvré
     * NON_RECOUVRE : Aucun montant recouvré
     */
    private String etatFinal;
    
    /**
     * Montant recouvré dans cette étape (juridique ou amiable)
     */
    private BigDecimal montantRecouvre;
}

