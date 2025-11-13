package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(optional = false) // Rend la relation obligatoire côté Java aussi
    @JoinColumn(name = "dossier_id", nullable = false) // Clé étrangère, non null
    @JsonIgnore // Évite la récursion infinie
    private Dossier dossier;

    // Relations avec les utilisateurs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_createur_id")
    private Utilisateur agentCreateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_responsable_id")
    private Utilisateur agentResponsable;

    // Champs transients pour accepter les IDs depuis le JSON
    @Transient
    @JsonProperty("agentCreateurId")
    private Long agentCreateurId;

    @Transient
    @JsonProperty("agentResponsableId")
    private Long agentResponsableId;

    @Transient
    @JsonProperty("dossierId")
    private Long dossierId;

    // Statut de l'enquête (même logique que Dossier)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private Statut statut = Statut.EN_ATTENTE_VALIDATION;

    // Propriétés de validation
    @Column(name = "valide")
    @Builder.Default
    private Boolean valide = false;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "commentaire_validation", length = 1000)
    private String commentaireValidation;

    // Relations avec les nouvelles entités
    @OneToMany(mappedBy = "enquete", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<ValidationEnquete> validations = new ArrayList<>();

    @OneToMany(mappedBy = "enquete", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<TacheUrgente> tachesUrgentes = new ArrayList<>();
}
