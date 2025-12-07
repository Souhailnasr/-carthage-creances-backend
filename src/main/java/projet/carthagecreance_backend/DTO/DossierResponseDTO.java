package projet.carthagecreance_backend.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO pour les réponses API des dossiers
 * Évite les boucles de référence infinie en ne sérialisant que les données nécessaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierResponseDTO {
    private Long id;
    private String titre;
    private String description;
    private String numeroDossier;
    private Double montantCreance;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date dateCreation;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date dateCloture;
    
    private String contratSigneFilePath;
    private String pouvoirFilePath;
    private Urgence urgence;
    private DossierStatus dossierStatus;
    private Statut statut;
    private TypeDocumentJustificatif typeDocumentJustificatif;
    private TypeRecouvrement typeRecouvrement;
    
    // Champs pour le recouvrement
    private Double montantTotal;
    private Double montantRecouvre;
    private Double montantRecouvrePhaseAmiable;  // ✅ NOUVEAU
    private Double montantRecouvrePhaseJuridique; // ✅ NOUVEAU
    private Double montantRestant;
    private EtatDossier etatDossier;
    
    // Informations simplifiées de l'avocat (sans la liste de dossiers)
    private AvocatInfo avocat;
    
    // Informations simplifiées de l'huissier (sans la liste de dossiers)
    private HuissierInfo huissier;
    
    // Informations simplifiées du créancier (sans la liste de dossiers)
    private CreancierInfo creancier;
    
    // Informations simplifiées du débiteur (sans la liste de dossiers)
    private DebiteurInfo debiteur;
    
    // Informations simplifiées de l'agent créateur
    private AgentInfo agentCreateur;
    
    // Informations simplifiées de l'agent responsable
    private AgentInfo agentResponsable;
    
    private Boolean valide;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateValidation;
    
    private String commentaireValidation;
    
    // Classes internes pour les informations simplifiées
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvocatInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String specialite;
        private String adresse;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HuissierInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String specialite;
        private String adresse;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreancierInfo {
        private Long id;
        private String codeCreancier;
        private String codeCreance;
        private String nom;
        private String prenom;
        private String adresse;
        private String ville;
        private String codePostal;
        private String telephone;
        private String fax;
        private String email;
        private Type type;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DebiteurInfo {
        private Long id;
        private String codeCreance;
        private String nom;
        private String prenom;
        private String adresseElue;
        private String ville;
        private String codePostal;
        private String telephone;
        private String fax;
        private String email;
        private Type type;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AgentInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private RoleUtilisateur roleUtilisateur;
    }
    
    /**
     * Convertit une entité Dossier en DTO de réponse
     */
    public static DossierResponseDTO fromEntity(Dossier dossier) {
        if (dossier == null) {
            return null;
        }
        
        DossierResponseDTO.DossierResponseDTOBuilder builder = DossierResponseDTO.builder()
                .id(dossier.getId())
                .titre(dossier.getTitre())
                .description(dossier.getDescription())
                .numeroDossier(dossier.getNumeroDossier())
                .montantCreance(dossier.getMontantCreance())
                .dateCreation(dossier.getDateCreation())
                .dateCloture(dossier.getDateCloture())
                .contratSigneFilePath(dossier.getContratSigneFilePath())
                .pouvoirFilePath(dossier.getPouvoirFilePath())
                .urgence(dossier.getUrgence())
                .dossierStatus(dossier.getDossierStatus())
                .statut(dossier.getStatut())
                .typeDocumentJustificatif(dossier.getTypeDocumentJustificatif())
                .typeRecouvrement(dossier.getTypeRecouvrement())
                .montantTotal(dossier.getMontantTotal())
                .montantRecouvre(dossier.getMontantRecouvre())
                .montantRecouvrePhaseAmiable(dossier.getMontantRecouvrePhaseAmiable())
                .montantRecouvrePhaseJuridique(dossier.getMontantRecouvrePhaseJuridique())
                .montantRestant(dossier.getMontantRestant())
                .etatDossier(dossier.getEtatDossier())
                .valide(dossier.getValide())
                .dateValidation(dossier.getDateValidation())
                .commentaireValidation(dossier.getCommentaireValidation());
        
        // Convertir l'avocat
        if (dossier.getAvocat() != null) {
            Avocat avocat = dossier.getAvocat();
            builder.avocat(AvocatInfo.builder()
                    .id(avocat.getId())
                    .nom(avocat.getNom())
                    .prenom(avocat.getPrenom())
                    .email(avocat.getEmail())
                    .telephone(avocat.getTelephone())
                    .specialite(avocat.getSpecialite())
                    .adresse(avocat.getAdresse())
                    .build());
        }
        
        // Convertir l'huissier
        if (dossier.getHuissier() != null) {
            Huissier huissier = dossier.getHuissier();
            builder.huissier(HuissierInfo.builder()
                    .id(huissier.getId())
                    .nom(huissier.getNom())
                    .prenom(huissier.getPrenom())
                    .email(huissier.getEmail())
                    .telephone(huissier.getTelephone())
                    .specialite(huissier.getSpecialite())
                    .adresse(huissier.getAdresse())
                    .build());
        }
        
        // Convertir le créancier
        if (dossier.getCreancier() != null) {
            Creancier creancier = dossier.getCreancier();
            builder.creancier(CreancierInfo.builder()
                    .id(creancier.getId())
                    .codeCreancier(creancier.getCodeCreancier())
                    .codeCreance(creancier.getCodeCreance())
                    .nom(creancier.getNom())
                    .prenom(creancier.getPrenom())
                    .adresse(creancier.getAdresse())
                    .ville(creancier.getVille())
                    .codePostal(creancier.getCodePostal())
                    .telephone(creancier.getTelephone())
                    .fax(creancier.getFax())
                    .email(creancier.getEmail())
                    .type(creancier.getType())
                    .build());
        }
        
        // Convertir le débiteur
        if (dossier.getDebiteur() != null) {
            Debiteur debiteur = dossier.getDebiteur();
            builder.debiteur(DebiteurInfo.builder()
                    .id(debiteur.getId())
                    .codeCreance(debiteur.getCodeCreance())
                    .nom(debiteur.getNom())
                    .prenom(debiteur.getPrenom())
                    .adresseElue(debiteur.getAdresseElue())
                    .ville(debiteur.getVille())
                    .codePostal(debiteur.getCodePostal())
                    .telephone(debiteur.getTelephone())
                    .fax(debiteur.getFax())
                    .email(debiteur.getEmail())
                    .type(debiteur.getType())
                    .build());
        }
        
        // Convertir l'agent créateur
        if (dossier.getAgentCreateur() != null) {
            Utilisateur agent = dossier.getAgentCreateur();
            builder.agentCreateur(AgentInfo.builder()
                    .id(agent.getId())
                    .nom(agent.getNom())
                    .prenom(agent.getPrenom())
                    .email(agent.getEmail())
                    .roleUtilisateur(agent.getRoleUtilisateur())
                    .build());
        }
        
        // Convertir l'agent responsable
        if (dossier.getAgentResponsable() != null) {
            Utilisateur agent = dossier.getAgentResponsable();
            builder.agentResponsable(AgentInfo.builder()
                    .id(agent.getId())
                    .nom(agent.getNom())
                    .prenom(agent.getPrenom())
                    .email(agent.getEmail())
                    .roleUtilisateur(agent.getRoleUtilisateur())
                    .build());
        }
        
        return builder.build();
    }
}

