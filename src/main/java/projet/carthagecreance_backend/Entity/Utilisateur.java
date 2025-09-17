package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.io.Serializable;
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


    @Enumerated(EnumType.STRING)
    private RoleUtilisateur roleUtilisateur;

    @ManyToMany(mappedBy = "utilisateurs")
    private List<Dossier> dossiers;
}
