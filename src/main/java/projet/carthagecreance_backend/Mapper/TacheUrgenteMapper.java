package projet.carthagecreance_backend.Mapper;

import org.springframework.stereotype.Component;
import projet.carthagecreance_backend.DTO.TacheUrgenteDTO;
import projet.carthagecreance_backend.Entity.TacheUrgente;

import java.time.LocalDateTime;

/**
 * Mapper pour convertir TacheUrgente en TacheUrgenteDTO
 */
@Component
public class TacheUrgenteMapper {
    
    public TacheUrgenteDTO toDTO(TacheUrgente tache) {
        if (tache == null) {
            return null;
        }
        
        TacheUrgenteDTO dto = TacheUrgenteDTO.builder()
                .id(tache.getId())
                .titre(tache.getTitre())
                .description(tache.getDescription())
                .type(tache.getType())
                .priorite(tache.getPriorite())
                .statut(tache.getStatut())
                .dateCreation(tache.getDateCreation())
                .dateEcheance(tache.getDateEcheance())
                .dateCompletion(tache.getDateCompletion())
                .commentaires(tache.getCommentaires())
                .build();
        
        // Agent assigné
        if (tache.getAgentAssigné() != null) {
            dto.setAgentAssignéId(tache.getAgentAssigné().getId());
            dto.setAgentAssignéNom(tache.getAgentAssigné().getNom());
            dto.setAgentAssignéPrenom(tache.getAgentAssigné().getPrenom());
        }
        
        // Chef créateur
        if (tache.getChefCreateur() != null) {
            dto.setChefCreateurId(tache.getChefCreateur().getId());
            dto.setChefCreateurNom(tache.getChefCreateur().getNom());
            dto.setChefCreateurPrenom(tache.getChefCreateur().getPrenom());
        }
        
        // Dossier
        if (tache.getDossier() != null) {
            dto.setDossierId(tache.getDossier().getId());
            dto.setDossierNumero(tache.getDossier().getNumeroDossier());
        }
        
        // Enquête
        if (tache.getEnquete() != null) {
            dto.setEnqueteId(tache.getEnquete().getId());
        }
        
        // Calculer si urgente (échéance dans les 3 jours)
        LocalDateTime maintenant = LocalDateTime.now();
        if (tache.getDateEcheance() != null) {
            dto.setEstUrgente(tache.getDateEcheance().isAfter(maintenant) && 
                             tache.getDateEcheance().isBefore(maintenant.plusDays(3)) &&
                             (tache.getStatut() == projet.carthagecreance_backend.Entity.StatutTache.EN_ATTENTE ||
                              tache.getStatut() == projet.carthagecreance_backend.Entity.StatutTache.EN_COURS));
            
            dto.setEstEnRetard(tache.getDateEcheance().isBefore(maintenant) &&
                              (tache.getStatut() == projet.carthagecreance_backend.Entity.StatutTache.EN_ATTENTE ||
                               tache.getStatut() == projet.carthagecreance_backend.Entity.StatutTache.EN_COURS));
        }
        
        return dto;
    }
}

