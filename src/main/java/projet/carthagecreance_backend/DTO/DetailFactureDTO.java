package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour le détail de la facture d'un dossier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailFactureDTO {
    private BigDecimal fraisCreationDossier;
    private BigDecimal fraisEnquete;  // ✅ NOUVEAU : Frais d'enquête (300 TND fixe + autres)
    private BigDecimal coutGestionTotal;
    private BigDecimal coutActionsAmiable;
    private BigDecimal coutActionsJuridique;
    private BigDecimal fraisAvocat;
    private BigDecimal fraisHuissier;
    
    // Commissions (selon annexe)
    private BigDecimal commissionAmiable;
    private BigDecimal commissionJuridique;
    
    // Totaux
    private BigDecimal totalHT;
    private BigDecimal tva;
    private BigDecimal totalTTC;
    private BigDecimal totalFacture;  // Alias pour totalTTC
}

