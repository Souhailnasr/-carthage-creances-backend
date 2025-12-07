package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PrioriteTache;
import projet.carthagecreance_backend.Entity.StatutTache;
import projet.carthagecreance_backend.Entity.TypeTache;

import java.time.LocalDateTime;

/**
 * DTO pour l'affichage d'une tâche urgente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TacheUrgenteDTO {
    private Long id;
    private String titre;
    private String description;
    private TypeTache type;
    private PrioriteTache priorite;
    private StatutTache statut;
    private Long agentAssignéId;
    private String agentAssignéNom;
    private String agentAssignéPrenom;
    private Long chefCreateurId;
    private String chefCreateurNom;
    private String chefCreateurPrenom;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateCompletion;
    private Long dossierId;
    private String dossierNumero;
    private Long enqueteId;
    private String commentaires;
    private boolean estUrgente; // Échéance dans les 3 jours
    private boolean estEnRetard; // Échéance passée
}

