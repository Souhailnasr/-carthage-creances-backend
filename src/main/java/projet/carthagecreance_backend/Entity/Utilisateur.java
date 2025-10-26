package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Utilisateur implements UserDetails {
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
    private String email;
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    @Builder.Default
    private Boolean actif = true;


    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date dateCreation;
    @Column(name = "derniere_connexion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime derniereConnexion;

    @Column(name = "derniere_deconnexion")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime derniereDeconnexion;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_Utilisateur")
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
    // Initialiser la date de création automatiquement
    @PrePersist
    protected void onCreate() {
        dateCreation = new java.util.Date();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // return new ArrayList<>();
        return Collections.singletonList(new SimpleGrantedAuthority(roleUtilisateur.getAuthority()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
