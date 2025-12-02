package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour un traitement (action, document, audience, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitementDTO {
    private Long id;
    private String type;  // Ex: "OUVERTURE_DOSSIER", "ENQUETE_PRECONTENTIEUSE", "APPEL_TELEPHONIQUE", etc.
    private LocalDate date;
    private Integer occurrences;  // Pour les actions
    private BigDecimal fraisFixe;  // Pour les frais fixes (250 TND création, 300 TND enquête)
    private BigDecimal coutUnitaire;  // Coût unitaire du tarif existant
    private TarifDossierDTO tarifExistant;  // Tarif existant si créé
    private String statut;  // Statut du tarif ou "EN_ATTENTE_TARIF"
}

