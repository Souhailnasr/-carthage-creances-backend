package projet.carthagecreance_backend.DTO;
import lombok.Getter;
import lombok.Setter;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Entity.DossierStatus;
import projet.carthagecreance_backend.Entity.TypeDocumentJustificatif; // Ajout

@Getter
@Setter

public class DossierRequest {
    private String titre;
    private String description;
    private String numeroDossier;
    private Double montantCreance;
    private String contratSigne; // Optionnel si upload séparé
    private String pouvoir;     // Optionnel si upload séparé
    private Urgence urgence;
    private DossierStatus dossierStatus;
    private TypeDocumentJustificatif typeDocumentJustificatif; // Ajout
    private String nomCreancier; // Nom du créancier
    private String nomDebiteur;  // Nom du débiteur
    private Long agentCreateurId; // ID de l'agent créateur (optionnel)
}
