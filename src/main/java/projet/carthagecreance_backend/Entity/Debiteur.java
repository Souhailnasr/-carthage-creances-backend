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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Debiteur implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le code créance est obligatoire")
    private String codeCreance;
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    private String prenom;
    private String adresseElue;
    private String ville;
    private String codePostal;
    @Pattern(regexp = "\\d{8}", message = "Le téléphone doit contenir exactement 8 chiffres")
    private String telephone;
    private String fax;
    @Email(message = "Email invalide")
    private String email;
    @Enumerated(EnumType.STRING)
    private Type type;
    // Relation corrigée : Un débiteur peut avoir plusieurs dossiers
    @OneToMany(mappedBy = "debiteur") // mappedBy pointe vers le champ 'debiteur' dans Dossier
    @JsonIgnore
    private List<Dossier> dossiers; // Liste des dossiers de ce débiteur


}
