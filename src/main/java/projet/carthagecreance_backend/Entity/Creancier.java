package projet.carthagecreance_backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Creancier implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le code créancier est obligatoire")
    private String codeCreancier;
    
    @NotBlank(message = "Le code créance est obligatoire")
    private String codeCreance;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    private String prenom;
    private String adresse;
    private String ville;
    private String codePostal;
    @Pattern(regexp = "\\d{8}", message = "Le téléphone doit contenir exactement 8 chiffres")
    private String telephone;
    private String fax;
    @Email(message = "Email invalide")
    private String email;
    @Enumerated(EnumType.STRING)
    private Type type;

    // Un créancier peut avoir plusieurs dossiers
    @OneToMany(mappedBy = "creancier") // mappedBy pointe vers le champ 'creancier' dans Dossier
    @JsonIgnore // Évite la récursion infinie lors de la sérialisation JSON
    private List<Dossier> dossiers; // Liste des dossiers de ce créancier
}

