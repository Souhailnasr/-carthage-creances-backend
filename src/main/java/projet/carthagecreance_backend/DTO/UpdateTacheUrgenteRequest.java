package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PrioriteTache;
import projet.carthagecreance_backend.Entity.TypeTache;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO pour mettre à jour une tâche urgente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTacheUrgenteRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    private String description;
    
    private TypeTache type;
    
    private PrioriteTache priorite;
    
    private Long agentAssignéId;
    
    private LocalDateTime dateEcheance;
    
    private Long dossierId;
    
    private Long enqueteId;
    
    private String commentaires;
}

