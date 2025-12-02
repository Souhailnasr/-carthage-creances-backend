package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pour vérifier si un dossier peut être clôturé
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeutEtreClotureDTO {
    private Boolean peutEtreCloture;
    private List<String> raisons;
    private Long factureId;
    private BigDecimal montantTTC;
    private BigDecimal totalPaiementsValides;
    private BigDecimal soldeRestant;
    private String statutFacture;
}

