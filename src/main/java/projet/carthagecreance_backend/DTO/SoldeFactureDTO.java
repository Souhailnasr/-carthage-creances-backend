package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour représenter le solde restant d'une facture
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldeFactureDTO {
    private Long factureId;
    private BigDecimal montantTTC;
    private BigDecimal totalPaiementsValides;
    private BigDecimal soldeRestant;
    private Boolean estEntierementPayee;
}

