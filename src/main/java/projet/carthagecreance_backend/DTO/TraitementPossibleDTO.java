package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour un traitement possible (optionnel) dans la phase enquête
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitementPossibleDTO {
    private String type;  // Ex: "EXPERTISE", "DEPLACEMENT", "AUTRES"
    private String libelle;  // Ex: "Expertise", "Déplacement", "Autres traitements"
    private TarifDossierDTO tarifExistant;  // Tarif existant si créé
    private String statut;  // Statut du tarif ou "EN_ATTENTE_TARIF"
}

