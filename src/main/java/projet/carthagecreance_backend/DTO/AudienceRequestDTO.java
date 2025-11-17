package projet.carthagecreance_backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.DecisionResult;
import projet.carthagecreance_backend.Entity.TribunalType;

import java.time.LocalDate;

/**
 * DTO pour la création et la mise à jour d'Audience
 * Permet de recevoir les IDs des relations au lieu des objets complets
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudienceRequestDTO {
    private LocalDate dateAudience;
    private LocalDate dateProchaine;
    private TribunalType tribunalType;
    private String lieuTribunal;
    private String commentaireDecision;
    private DecisionResult resultat;
    
    // IDs des relations (au lieu des objets complets)
    @JsonProperty("dossierId")
    private Long dossierId;
    
    // Support pour l'ancien format { "dossier": { "id": 38 } }
    @JsonProperty("dossier")
    private DossierReference dossier;
    
    @JsonProperty("avocatId")
    private Long avocatId;
    
    @JsonProperty("avocat")
    private AvocatReference avocat;
    
    @JsonProperty("huissierId")
    private Long huissierId;
    
    @JsonProperty("huissier")
    private HuissierReference huissier;
    
    /**
     * Classe interne pour désérialiser { "dossier": { "id": 38 } }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DossierReference {
        private Long id;
    }
    
    /**
     * Classe interne pour désérialiser { "avocat": { "id": 3 } }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvocatReference {
        private Long id;
    }
    
    /**
     * Classe interne pour désérialiser { "huissier": { "id": 2 } }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HuissierReference {
        private Long id;
    }
    
    /**
     * Récupère l'ID du dossier (depuis dossierId ou dossier.id)
     */
    public Long getDossierIdValue() {
        if (dossierId != null) {
            return dossierId;
        }
        if (dossier != null && dossier.getId() != null) {
            return dossier.getId();
        }
        return null;
    }
    
    /**
     * Récupère l'ID de l'avocat (depuis avocatId ou avocat.id)
     */
    public Long getAvocatIdValue() {
        if (avocatId != null) {
            return avocatId;
        }
        if (avocat != null && avocat.getId() != null) {
            return avocat.getId();
        }
        return null;
    }
    
    /**
     * Récupère l'ID de l'huissier (depuis huissierId ou huissier.id)
     */
    public Long getHuissierIdValue() {
        if (huissierId != null) {
            return huissierId;
        }
        if (huissier != null && huissier.getId() != null) {
            return huissier.getId();
        }
        return null;
    }
}

