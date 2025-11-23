package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.ModePaiement;
import projet.carthagecreance_backend.Entity.StatutPaiement;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementDTO {
    private Long id;
    private Long factureId;
    private String factureNumero;
    private LocalDate datePaiement;
    private Double montant;
    private ModePaiement modePaiement;
    private String reference;
    private StatutPaiement statut;
    private String commentaire;
}

