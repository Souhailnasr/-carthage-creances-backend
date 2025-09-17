package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
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


    // Pièces à joindre (chemins fichiers ou liens)
    private String contratSigne;
    private String pouvoir;

    @Enumerated(EnumType.STRING)
    private Urgence urgence;
    @Enumerated(EnumType.STRING)
    private DossierStatus dossierStatus;

    @Enumerated(EnumType.STRING)
    private TypeDocumentJustificatif typeDocumentJustificatif;
    // Utilisateurs associés
    @ManyToMany

    private List<Utilisateur> utilisateurs;

    // Enquête (exactement 1)
    @OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
    private Enquette enquette;

    // Audiences (1 ou plusieurs)
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL)
    private List<Audience> audiences;

    // Avocat et Huissier (optionnels)
    @ManyToOne
    private Avocat avocat;

    @ManyToOne
    private Huissier huissier;

    // Finance, Créancier et Débiteur
    @OneToOne(mappedBy = "dossier", cascade = CascadeType.ALL)
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
    private List<Action> actions;



    // Initialiser la date de création automatiquement
    @PrePersist
    protected void onCreate() {
        dateCreation = new java.util.Date();
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
}

