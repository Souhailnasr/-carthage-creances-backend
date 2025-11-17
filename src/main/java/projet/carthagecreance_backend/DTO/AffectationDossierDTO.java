package projet.carthagecreance_backend.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO pour l'affectation d'un dossier à un avocat et/ou un huissier
 * Permet d'affecter soit un avocat, soit un huissier, soit les deux
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffectationDossierDTO {
    /**
     * ID de l'avocat à affecter (optionnel)
     * Si null, l'avocat actuel sera retiré si présent
     */
    private Long avocatId;
    
    /**
     * ID de l'huissier à affecter (optionnel)
     * Si null, l'huissier actuel sera retiré si présent
     */
    private Long huissierId;
}

