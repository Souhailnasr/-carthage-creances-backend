package projet.carthagecreance_backend.Mapper;

import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import projet.carthagecreance_backend.DTO.FactureDTO;
import projet.carthagecreance_backend.Entity.Facture;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir Facture en FactureDTO
 * Gère le mapping du dossierId et du dossierNumero
 */
@Component
public class FactureMapper {
    
    /**
     * Convertit une entité Facture en DTO
     * 
     * @param facture L'entité Facture à convertir
     * @return Le DTO correspondant
     */
    public FactureDTO toDTO(Facture facture) {
        if (facture == null) {
            return null;
        }
        
        FactureDTO dto = FactureDTO.builder()
                .id(facture.getId())
                .numeroFacture(facture.getNumeroFacture())
                // ✅ CRITIQUE : Mapper le dossierId depuis la relation Dossier
                .dossierId(facture.getDossierId())
                // ✅ Optionnel : Mapper le numéro de dossier
                .dossierNumero(facture.getNumeroDossier())
                .periodeDebut(facture.getPeriodeDebut())
                .periodeFin(facture.getPeriodeFin())
                .dateEmission(facture.getDateEmission())
                .dateEcheance(facture.getDateEcheance())
                .montantHT(facture.getMontantHT())
                .montantTTC(facture.getMontantTTC())
                .tva(facture.getTva())
                .statut(facture.getStatut())
                .pdfUrl(facture.getPdfUrl())
                .envoyee(facture.getEnvoyee())
                .relanceEnvoyee(facture.getRelanceEnvoyee())
                // Note: fluxFrais et paiements sont ignorés pour éviter la récursion
                // Si nécessaire, créer des DTOs séparés pour ces relations
                .build();
        
        return dto;
    }
    
    /**
     * Convertit une liste de Facture en liste de FactureDTO
     * 
     * @param factures La liste d'entités Facture
     * @return La liste de DTOs correspondants
     */
    public List<FactureDTO> toDTOList(List<Facture> factures) {
        if (factures == null) {
            return null;
        }
        return factures.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convertit une Page de Facture en Page de FactureDTO
     * 
     * @param page La page d'entités Facture
     * @return La page de DTOs correspondants
     */
    public Page<FactureDTO> toDTOPage(Page<Facture> page) {
        if (page == null) {
            return null;
        }
        return page.map(this::toDTO);
    }
}

