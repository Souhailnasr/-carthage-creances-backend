package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enquette implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rapportCode; // Exemple CC6008

    // Éléments financiers
    private String nomElementFinancier;
    private Double pourcentage;
    private String banqueAgence;
    private String banques;
    private String exercices;
    private Double chiffreAffaire;
    private Double resultatNet;
    private String disponibiliteBilan;

    // Solvabilité
    private String appreciationBancaire;
    private String paiementsCouverture;
    private String reputationCommerciale;
    private String incidents;

    // Patrimoine débiteur
    private String bienImmobilier;
    private String situationJuridiqueImmobilier;
    private String bienMobilier;
    private String situationJuridiqueMobilier;

    // Autres affaires & observations
    private String autresAffaires;
    private String observations;

    // Décision comité recouvrement
    private String decisionComite;
    private String visaDirecteurJuridique;
    private String visaEnqueteur;
    private String visaDirecteurCommercial;
    private String registreCommerce;
    private String codeDouane;
    private String matriculeFiscale;
    private String formeJuridique;
    private LocalDate dateCreation;
    private Double capital;

    // Dirigeants
    private String pdg;
    private String directeurAdjoint;
    private String directeurFinancier;
    private String directeurCommercial;

    // Activité
    private String descriptionActivite;
    private String secteurActivite;
    private Integer effectif;

    // Informations diverses
    private String email;
    private String marques;
    private String groupe;

    @OneToOne
    @JoinColumn(name = "dossier_id")  // clé étrangère
    private Dossier dossier;
}
