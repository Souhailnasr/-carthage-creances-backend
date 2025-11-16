package projet.carthagecreance_backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import projet.carthagecreance_backend.Entity.TypeAction;
import projet.carthagecreance_backend.Entity.ReponseDebiteur;

import java.time.LocalDate;

/**
 * DTO pour la création et la mise à jour d'Action
 * Permet de recevoir dossierId au lieu de l'objet Dossier complet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRequestDTO {
    private TypeAction type;
    private LocalDate dateAction;
    private Integer nbOccurrences;
    private Double coutUnitaire; // Envoyé par le frontend, calculé dans Finance
    private ReponseDebiteur reponseDebiteur; // Peut être null
    private Long dossierId; // ID du dossier au lieu de l'objet Dossier
}

