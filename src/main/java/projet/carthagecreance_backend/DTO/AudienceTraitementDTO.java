package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour une audience dans les traitements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudienceTraitementDTO {
    private Long id;
    private LocalDate date;
    private String type;  // Type d'audience
    private Long avocatId;
    private String avocatNom;
    private BigDecimal coutAudience;  // Coût de l'audience (du tarif existant)
    private BigDecimal coutAvocat;  // Coût de l'avocat (du tarif existant)
    private TarifDossierDTO tarifAudience;  // Tarif pour l'audience
    private TarifDossierDTO tarifAvocat;  // Tarif pour l'avocat
    private String statut;  // Statut des tarifs
}

