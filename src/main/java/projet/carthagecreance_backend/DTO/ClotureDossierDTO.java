package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse de clôture et archivage d'un dossier
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClotureDossierDTO {
    private Long dossierId;
    private String statut;
    private LocalDateTime dateCloture;
    private Boolean archive;
    private LocalDateTime dateArchivage;
    private String message;
}

