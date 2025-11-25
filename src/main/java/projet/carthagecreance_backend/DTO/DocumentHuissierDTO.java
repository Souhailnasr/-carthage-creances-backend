package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import projet.carthagecreance_backend.Entity.TypeDocumentHuissier;
import projet.carthagecreance_backend.Entity.StatutDocumentHuissier;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentHuissierDTO {
    private Long id;
    private Long dossierId;
    private TypeDocumentHuissier typeDocument;
    private Instant dateCreation;
    private Integer delaiLegalDays;
    private String pieceJointeUrl;
    private String huissierName;
    private StatutDocumentHuissier status;
    private Boolean notified;
}
