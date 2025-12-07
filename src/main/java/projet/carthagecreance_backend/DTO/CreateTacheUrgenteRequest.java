package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PrioriteTache;
import projet.carthagecreance_backend.Entity.TypeTache;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO pour créer une tâche urgente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTacheUrgenteRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    private String description;
    
    @NotNull(message = "Le type est obligatoire")
    private TypeTache type;
    
    @NotNull(message = "La priorité est obligatoire")
    private PrioriteTache priorite;
    
    @NotNull(message = "L'agent assigné est obligatoire")
    private Long agentAssignéId;
    
    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDateTime dateEcheance;
    
    private Long dossierId;
    private Long enqueteId;
}

