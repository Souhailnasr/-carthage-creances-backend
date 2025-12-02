package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO pour un document huissier dans les traitements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentHuissierTraitementDTO {
    private Long id;
    private String type;  // Type de document
    private Instant date;
    private BigDecimal coutUnitaire;  // Coût unitaire du tarif existant
    private TarifDossierDTO tarifExistant;
    private String statut;  // Statut du tarif ou "EN_ATTENTE_TARIF"
}

