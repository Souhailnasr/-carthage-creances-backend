package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class Dossier implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String numeroDossier;
    private Double montantCreance;

    // Dates
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date dateCreation;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date dateCloture;


    // Pièces à joindre (chemins fichiers)
    @Column(name = "contrat_signe_file_path")
    private String contratSigneFilePath;
    
    @Column(name = "pouvoir_file_path")
    private String pouvoirFilePath;

    @Enumerated(EnumType.STRING)
    private Urgence urgence;
    @Enumerated(EnumType.STRING)
    private DossierStatus dossierStatus;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @Enumerated(EnumType.STRING)
    private TypeDocumentJustificatif typeDocumentJustificatif;
    // Utilisateurs associés
    @ManyToMany

    private List<Utilisateur> utilisateurs;

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


