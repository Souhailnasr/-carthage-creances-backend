package projet.carthagecreance_backend.DTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
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
    private MultipartFile contratSigneFile; // Fichier contrat signé
    private MultipartFile pouvoirFile;       // Fichier pouvoir
    private String contratSigneFilePath;    // Chemin du fichier contrat signé
    private String pouvoirFilePath;          // Chemin du fichier pouvoir
    private Urgence urgence;
    private DossierStatus dossierStatus;
    private TypeDocumentJustificatif typeDocumentJustificatif; // Ajout
    private String nomCreancier; // Nom du créancier
    private String nomDebiteur;  // Nom du débiteur
    private Long agentCreateurId; // ID de l'agent créateur (optionnel)
    
    // Getters et setters pour les fichiers
    public MultipartFile getContratSigneFile() {
        return contratSigneFile;
    }
    
    public void setContratSigneFile(MultipartFile contratSigneFile) {
        this.contratSigneFile = contratSigneFile;
    }
    
    public MultipartFile getPouvoirFile() {
        return pouvoirFile;
    }
    
    public void setPouvoirFile(MultipartFile pouvoirFile) {
        this.pouvoirFile = pouvoirFile;
    }
    
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
