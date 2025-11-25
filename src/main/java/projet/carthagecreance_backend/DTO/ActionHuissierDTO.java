package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.TypeActionHuissier;
import projet.carthagecreance_backend.Entity.ModeMiseAJour;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionHuissierDTO {
    private Long id;
    private Long dossierId;
    private TypeActionHuissier typeAction;
    private BigDecimal montantRecouvre;
    private BigDecimal montantRestant;
    private String etatDossier;
    private Instant dateAction;
    private String pieceJointeUrl;
    private String huissierName;
    private ModeMiseAJour updateMode; // ADD ou SET
}

