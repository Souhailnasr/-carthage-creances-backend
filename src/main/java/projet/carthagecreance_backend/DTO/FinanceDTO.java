package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour l'entité Finance
 * Inclut le dossierId pour faciliter l'accès depuis le frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDTO {
    private Long id;
    
    // ✅ CRITIQUE : dossierId pour le frontend
    private Long dossierId;
    
    // ✅ Optionnel : numéro de dossier pour l'affichage
    private String numeroDossier;
    
    private String devise;
    private LocalDate dateOperation;
    private String description;
    
    // Frais Avocat & Huissier
    private Double fraisAvocat;
    private Double fraisHuissier;
    
    // Coûts de création et gestion
    private Double fraisCreationDossier;
    private Double fraisGestionDossier;
    private Integer dureeGestionMois;
    
    // Coûts des actions
    private Double coutActionsAmiable;
    private Double coutActionsJuridique;
    private Integer nombreActionsAmiable;
    private Integer nombreActionsJuridique;
    
    // Statut de facturation
    private Boolean factureFinalisee;
    private LocalDate dateFacturation;
    
    // Calculs (optionnels, peuvent être calculés côté frontend)
    private Double totalActions;
    private Double totalGlobal;
    private Double coutTotalActions;
    private Double coutGestionTotal;
    private Double factureFinale;
}

