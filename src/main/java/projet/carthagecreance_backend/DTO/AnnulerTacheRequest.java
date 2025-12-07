package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour annuler une tâche
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnulerTacheRequest {
    @NotBlank(message = "La raison d'annulation est obligatoire")
    private String raison;
}

