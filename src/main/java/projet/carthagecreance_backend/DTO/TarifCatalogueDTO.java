package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.PhaseFrais;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarifCatalogueDTO {
    private Long id;
    private PhaseFrais phase;
    private String categorie;
    private String description;
    private String fournisseur;
    private Double tarifUnitaire;
    private String devise;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif;
}

