package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO pour une action huissier dans les traitements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionHuissierTraitementDTO {
    private Long id;
    private String type;  // Type d'action huissier
    private Instant date;
    private BigDecimal coutUnitaire;  // Coût unitaire du tarif existant
    private TarifDossierDTO tarifExistant;
    private String statut;  // Statut du tarif ou "EN_ATTENTE_TARIF"
}

