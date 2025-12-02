package projet.carthagecreance_backend.Mapper;

import org.springframework.stereotype.Component;
import projet.carthagecreance_backend.DTO.FinanceDTO;
import projet.carthagecreance_backend.Entity.Finance;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir Finance en FinanceDTO
 * Gère le mapping du dossierId et du numeroDossier
 */
@Component
public class FinanceMapper {
    
    /**
     * Convertit une entité Finance en DTO
     * 
     * @param finance L'entité Finance à convertir
     * @return Le DTO correspondant
     */
    public FinanceDTO toDTO(Finance finance) {
        if (finance == null) {
            return null;
        }
        
        FinanceDTO dto = FinanceDTO.builder()
                .id(finance.getId())
                // ✅ CRITIQUE : Mapper le dossierId
                .dossierId(finance.getDossierId())
                // ✅ Optionnel : Mapper le numéro de dossier
                .numeroDossier(finance.getNumeroDossier())
                .devise(finance.getDevise())
                .dateOperation(finance.getDateOperation())
                .description(finance.getDescription())
                .fraisAvocat(finance.getFraisAvocat())
                .fraisHuissier(finance.getFraisHuissier())
                .fraisCreationDossier(finance.getFraisCreationDossier())
                .fraisGestionDossier(finance.getFraisGestionDossier())
                .dureeGestionMois(finance.getDureeGestionMois())
                .coutActionsAmiable(finance.getCoutActionsAmiable())
                .coutActionsJuridique(finance.getCoutActionsJuridique())
                .nombreActionsAmiable(finance.getNombreActionsAmiable())
                .nombreActionsJuridique(finance.getNombreActionsJuridique())
                .factureFinalisee(finance.getFactureFinalisee())
                .dateFacturation(finance.getDateFacturation())
                // Calculs optionnels
                .totalActions(finance.calculerTotalActions())
                .totalGlobal(finance.calculerTotalGlobal())
                .coutTotalActions(finance.calculerCoutTotalActions())
                .coutGestionTotal(finance.calculerCoutGestionTotal())
                .factureFinale(finance.calculerFactureFinale())
                .build();
        
        return dto;
    }
    
    /**
     * Convertit une liste de Finance en liste de FinanceDTO
     * 
     * @param finances La liste d'entités Finance
     * @return La liste de DTOs correspondants
     */
    public List<FinanceDTO> toDTOList(List<Finance> finances) {
        if (finances == null) {
            return null;
        }
        return finances.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convertit une Page de Finance en Page de FinanceDTO
     * 
     * @param page La page d'entités Finance
     * @return La page de DTOs correspondants
     */
    public Page<FinanceDTO> toDTOPage(Page<Finance> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toDTO);
    }
}

