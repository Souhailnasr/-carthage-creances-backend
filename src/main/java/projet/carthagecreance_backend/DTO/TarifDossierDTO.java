package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.StatutTarif;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité TarifDossier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifDossierDTO {
    private Long id;
    private Long dossierId;
    private PhaseFrais phase;
    private String categorie;
    private String typeElement;
    private BigDecimal coutUnitaire;
    private Integer quantite;
    private BigDecimal montantTotal;
    private StatutTarif statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidation;
    private String commentaire;
    
    // IDs des relations optionnelles
    private Long documentHuissierId;
    private Long actionHuissierId;
    private Long audienceId;
    private Long actionId;
    private Long enqueteId;
}

