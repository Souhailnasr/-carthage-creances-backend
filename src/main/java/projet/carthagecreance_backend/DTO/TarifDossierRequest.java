package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PhaseFrais;

import java.math.BigDecimal;

/**
 * DTO pour la création d'un tarif de dossier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifDossierRequest {
    private PhaseFrais phase;
    private String categorie;
    private String typeElement;
    private BigDecimal coutUnitaire;
    private Integer quantite;
    private String commentaire;
    
    // IDs optionnels pour lier le tarif à un traitement spécifique
    private Long documentHuissierId;
    private Long actionHuissierId;
    private Long audienceId;
    private Long actionId;
    private Long enqueteId;
}

