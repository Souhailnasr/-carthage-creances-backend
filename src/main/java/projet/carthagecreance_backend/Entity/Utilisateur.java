package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(unique = true) // Assure l'unicité de l'email
    private String email;
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "derniere_deconnexion")
    private LocalDateTime derniereDeconnexion;

    @Enumerated(EnumType.STRING)
    private RoleUtilisateur roleUtilisateur;

    @ManyToMany(mappedBy = "utilisateurs")
    @JsonIgnore // Évite la récursion infinie
    private List<Dossier> dossiers;
    
    @OneToMany(mappedBy = "agentCreateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<Dossier> dossiersCrees = new ArrayList<>();

    @OneToMany(mappedBy = "agentResponsable", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<Dossier> dossiersAssignes = new ArrayList<>();

    @OneToMany(mappedBy = "agentCreateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<Enquette> enquetesCrees = new ArrayList<>();

    @OneToMany(mappedBy = "agentResponsable", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<Enquette> enquetesAssignes = new ArrayList<>();

    @OneToMany(mappedBy = "agentAssigné", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<TacheUrgente> tachesUrgentes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Évite la récursion infinie
    private List<PerformanceAgent> performances = new ArrayList<>();
}
