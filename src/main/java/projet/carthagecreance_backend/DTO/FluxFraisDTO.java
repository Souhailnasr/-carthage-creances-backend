package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PhaseFrais;
import projet.carthagecreance_backend.Entity.StatutFrais;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FluxFraisDTO {
    private Long id;
    private PhaseFrais phase;
    private String categorie;
    private Integer quantite;
    private Double tarifUnitaire;
    private Double montant;
    private StatutFrais statut;
    private LocalDate dateAction;
    private String justificatifUrl;
    private String commentaire;
    
    // IDs des relations
    private Long dossierId;
    private Long actionId;
    private Long enqueteId;
    private Long audienceId;
    private Long avocatId;
    private Long huissierId;
    private Long factureId;
    
    // Informations pour affichage
    private String dossierNumero;
    private String agentNom;
}

