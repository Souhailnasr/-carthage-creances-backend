package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour une action amiable dans les traitements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionAmiableTraitementDTO {
    private Long id;
    private String type;  // Type d'action (APPEL, EMAIL, VISITE, LETTRE, etc.)
    private LocalDate date;
    private Integer occurrences;
    private BigDecimal coutUnitaire;  // Coût unitaire du tarif existant
    private TarifDossierDTO tarifExistant;
    private String statut;  // Statut du tarif ou "EN_ATTENTE_TARIF"
}

