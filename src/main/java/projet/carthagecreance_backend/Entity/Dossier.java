package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dossier implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String numeroDossier;
    private Double montantCreance;
    
    // Champs pour le recouvrement
    @Column(name = "montant_total")
    private Double montantTotal;
    
    @Column(name = "montant_recouvre")
    @Builder.Default
    private Double montantRecouvre = 0.0;
    
    @Column(name = "montant_restant")
    private Double montantRestant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "etat_dossier")
    private EtatDossier etatDossier;
    
    // ✅ Champs pour la prédiction IA
    @Column(name = "etat_prediction")
    @Enumerated(EnumType.STRING)
    private EtatDossier etatPrediction; // État prédit par l'IA
    
    @Column(name = "risk_score")
    private Double riskScore; // Score de risque (0-100)
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel; // Niveau de risque: "Faible", "Moyen", "Élevé"

    // Dates
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date dateCreation;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date dateCloture;
    
    // ✅ Champs pour l'archivage
    @Column(name = "archive")
    @Builder.Default
    private Boolean archive = false;
    
    @Column(name = "date_archivage")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date dateArchivage;


    // Pièces à joindre (chemins fichiers)
    @Column(name = "contrat_signe_file_path")
    private String contratSigneFilePath;
    
    @Column(name = "pouvoir_file_path")
    private String pouvoirFilePath;

    @Enumerated(EnumType.STRING)
    private Urgence urgence;
    @Enumerated(EnumType.STRING)
    @Column(name = "dossier_status", nullable = false)
    @NotNull
    @Builder.Default
    private DossierStatus dossierStatus = DossierStatus.ENCOURSDETRAITEMENT;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @Enumerated(EnumType.STRING)
    private TypeDocumentJustificatif typeDocumentJustificatif;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_recouvrement", nullable = true)
    @Builder.Default
    private TypeRecouvrement typeRecouvrement = TypeRecouvrement.NON_AFFECTE;
    
    // Workflow Huissier
    @Enumerated(EnumType.STRING)
    @Column(name = "etape_huissier", nullable = false)
    @Builder.Default
    private EtapeHuissier etapeHuissier = EtapeHuissier.EN_ATTENTE_DOCUMENTS;
    
    // Utilisateurs associés
    @ManyToMany
    @JoinTable(
        name = "dossier_utilisateurs",
        joinColumns = @JoinColumn(name = "dossier_id"),
        inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    @Builder.Default
    private List<Utilisateur> utilisateurs = new ArrayList<>();

    // Enquête (exactement 1)
    @OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore // Évite la récursion infinie
    private Enquette enquette;

    // Audiences (1 ou plusieurs)
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore // Évite la récursion infinie
    private List<Audience> audiences;

    // Avocat et Huissier (optionnels)
    @ManyToOne
    private Avocat avocat;

    @ManyToOne
    private Huissier huissier;

    // Finance, Créancier et Débiteur
    @OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore // Évite la récursion infinie
    private Finance finance;

    // Relations : Many Dossiers to One Creancier/Debiteur
    @ManyToOne(optional = false) // Le dossier doit avoir un créancier
    @JoinColumn(name = "creancier_id", nullable = false) // Colonne FK dans la table Dossier
    private Creancier creancier;

    @ManyToOne(optional = false) // Le dossier doit avoir un débiteur
    @JoinColumn(name = "debiteur_id", nullable = false) // Colonne FK dans la table Dossier
    private Debiteur debiteur;

    // Actions (1 ou plusieurs)
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    @JsonIgnore // Évite la récursion infinie
    private List<Action> actions;


    // Initialiser la date de création automatiquement
    @PrePersist
    protected void onCreate() {
        dateCreation = new java.util.Date();
        if (statut == null) {
            statut = Statut.EN_ATTENTE_VALIDATION;
        }
        if (etapeHuissier == null) {
            etapeHuissier = EtapeHuissier.EN_ATTENTE_DOCUMENTS;
        }
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_createur_id")
    private Utilisateur agentCreateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_responsable_id")
    private Utilisateur agentResponsable;

    @Column(name = "valide")
    @Builder.Default
    private Boolean valide = false;

    @Column(name = "date_validation")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateValidation;

    @Column(name = "commentaire_validation", length = 1000)
    private String commentaireValidation;

    // Relations avec les nouvelles entités
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<ValidationDossier> validations = new ArrayList<>();

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<TacheUrgente> tachesUrgentes = new ArrayList<>();
    
    // Getters et setters spécifiques pour les chemins de fichiers
    public String getContratSigneFilePath() {
        return contratSigneFilePath;
    }
    
    public void setContratSigneFilePath(String contratSigneFilePath) {
        this.contratSigneFilePath = contratSigneFilePath;
    }
    
    public String getPouvoirFilePath() {
        return pouvoirFilePath;
    }
    
    public void setPouvoirFilePath(String pouvoirFilePath) {
        this.pouvoirFilePath = pouvoirFilePath;
    }
}
    /*

    // Valider les pièces et attributs obligatoires
     @PrePersist
      @PreUpdate
     private void validateDossier() {
        if (numeroDossier == null || montantCreance == null || contratSigne == null || pouvoir == null) {
             throw new RuntimeException("Le numéro de dossier, le montant et toutes les pièces sont obligatoires.");
        }
    } */


