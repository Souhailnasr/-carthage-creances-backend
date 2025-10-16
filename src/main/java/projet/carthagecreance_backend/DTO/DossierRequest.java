package projet.carthagecreance_backend.DTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.Entity.Urgence;
import projet.carthagecreance_backend.Entity.DossierStatus;
import projet.carthagecreance_backend.Entity.Statut;
import projet.carthagecreance_backend.Entity.TypeDocumentJustificatif; // Ajout
import projet.carthagecreance_backend.Entity.Type; // Ajout type personne

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
    private Statut statut; // optionnel: statut du workflow (EN_ATTENTE_VALIDATION, VALIDE, REJETE, EN_COURS, CLOTURE)
    private TypeDocumentJustificatif typeDocumentJustificatif; // Ajout
    // Créancier
    private Type typeCreancier; // PERSONNE_PHYSIQUE ou PERSONNE_MORALE
    private String nomCreancier; // Obligatoire pour MORALE et PHYSIQUE
    private String prenomCreancier; // Obligatoire si PHYSIQUE
    private String codeCreancier; // optionnel
    private String codeCreanceCreancier; // optionnel

    // Débiteur
    private Type typeDebiteur; // PERSONNE_PHYSIQUE ou PERSONNE_MORALE
    private String nomDebiteur;  // Obligatoire pour MORALE et PHYSIQUE
    private String prenomDebiteur; // Obligatoire si PHYSIQUE
    private String codeCreanceDebiteur; // optionnel
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
