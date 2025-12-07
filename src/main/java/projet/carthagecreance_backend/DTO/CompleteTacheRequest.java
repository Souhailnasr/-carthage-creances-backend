package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour compléter une tâche
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteTacheRequest {
    private String commentaire;
}

