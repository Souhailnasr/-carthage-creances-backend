package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.FactureStatut;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureDTO {
    private Long id;
    private String numeroFacture;
    private Long dossierId;
    private String dossierNumero;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private Double montantHT;
    private Double montantTTC;
    private Double tva;
    private FactureStatut statut;
    private String pdfUrl;
    private Boolean envoyee;
    private Boolean relanceEnvoyee;
    private List<FluxFraisDTO> fluxFrais;
    private List<PaiementDTO> paiements;
}

