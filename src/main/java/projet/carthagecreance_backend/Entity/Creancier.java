package projet.carthagecreance_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
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
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    private String adresse;
    private String ville;
    private String codePostal;
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "\\d{8}", message = "Le téléphone doit contenir exactement 8 chiffres")
    private String telephone;
    private String fax;
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    // Relation corrigée : Un créancier peut avoir plusieurs dossiers
    @OneToMany(mappedBy = "creancier") // mappedBy pointe vers le champ 'creancier' dans Dossier
    private List<Dossier> dossiers; // Liste des dossiers de ce créancier
}

