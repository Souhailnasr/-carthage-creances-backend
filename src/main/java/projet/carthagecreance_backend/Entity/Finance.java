package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finance implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String devise;
    private LocalDate dateOperation;
    private String description;

    // ✅ Frais Avocat & Huissier
    private Double fraisAvocat;
    private Double fraisHuissier;
    
    // ✅ Coûts de création et gestion
    @Column(name = "frais_creation_dossier")
    @Builder.Default
    private Double fraisCreationDossier = 50.0;
    
    @Column(name = "frais_gestion_dossier")
    @Builder.Default
    private Double fraisGestionDossier = 10.0;
    
    @Column(name = "duree_gestion_mois")
    @Builder.Default
    private Integer dureeGestionMois = 0;
    
    // ✅ Coûts des actions
    @Column(name = "cout_actions_amiable")
    @Builder.Default
    private Double coutActionsAmiable = 0.0;
    
    @Column(name = "cout_actions_juridique")
    @Builder.Default
    private Double coutActionsJuridique = 0.0;
    
    @Column(name = "nombre_actions_amiable")
    @Builder.Default
    private Integer nombreActionsAmiable = 0;
    
    @Column(name = "nombre_actions_juridique")
    @Builder.Default
    private Integer nombreActionsJuridique = 0;
    
    // ✅ Statut de facturation
    @Column(name = "facture_finalisee")
    @Builder.Default
    private Boolean factureFinalisee = false;
    
    @Column(name = "date_facturation")
    private LocalDate dateFacturation;

    // ✅ Relation avec Dossier
    @OneToOne
    @JoinColumn(name = "dossier_id")
    @JsonIgnore // Évite la récursion infinie
    private Dossier dossier;

    // ✅ Liste des Actions liées à cette Finance
    @OneToMany(mappedBy = "finance", cascade = CascadeType.ALL)
    @JsonIgnore // Évite la récursion infinie
    private List<Action> actions;
    
    // ✅ Statut de validation des tarifs
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation_tarifs")
    @Builder.Default
    private StatutValidationTarifs statutValidationTarifs = StatutValidationTarifs.EN_COURS;
    
    // Note: Les tarifs sont liés au Dossier, pas directement à Finance
    // On peut les récupérer via TarifDossierRepository.findByDossierId(dossierId)

    // ✅ Calcul total des frais des actions
    public Double calculerTotalActions() {
        if (actions == null || actions.isEmpty()) return 0.0;
        return actions.stream()
                .mapToDouble(Action::getTotalCout)
                .sum();
    }

    // ✅ Calcul total global (Actions + Avocat + Huissier)
    public Double calculerTotalGlobal() {
        double totalActions = calculerTotalActions();
        double totalAvocat = (fraisAvocat != null ? fraisAvocat : 0.0);
        double totalHuissier = (fraisHuissier != null ? fraisHuissier : 0.0);
        return totalActions + totalAvocat + totalHuissier;
    }
    
    // ✅ Calcul coût total des actions (amiable + juridique)
    public Double calculerCoutTotalActions() {
        double totalAmiable = (coutActionsAmiable != null ? coutActionsAmiable : 0.0);
        double totalJuridique = (coutActionsJuridique != null ? coutActionsJuridique : 0.0);
        return totalAmiable + totalJuridique;
    }
    
    // ✅ Calcul coût total de gestion
    public Double calculerCoutGestionTotal() {
        double fraisGestion = (fraisGestionDossier != null ? fraisGestionDossier : 0.0);
        int duree = (dureeGestionMois != null ? dureeGestionMois : 0);
        return fraisGestion * duree;
    }
    
    // ✅ Calcul facture finale complète
    public Double calculerFactureFinale() {
        double fraisCreation = (fraisCreationDossier != null ? fraisCreationDossier : 0.0);
        double coutGestion = calculerCoutGestionTotal();
        double coutActions = calculerCoutTotalActions();
        double fraisAvocat = (this.fraisAvocat != null ? this.fraisAvocat : 0.0);
        double fraisHuissier = (this.fraisHuissier != null ? this.fraisHuissier : 0.0);
        
        return fraisCreation + coutGestion + coutActions + fraisAvocat + fraisHuissier;
    }
    
    // ✅ Méthode utilitaire pour obtenir le dossierId
    public Long getDossierId() {
        return dossier != null ? dossier.getId() : null;
    }
    
    // ✅ Méthode utilitaire pour obtenir le numéro de dossier
    public String getNumeroDossier() {
        return dossier != null ? dossier.getNumeroDossier() : null;
    }
}

