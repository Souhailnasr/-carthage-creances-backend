package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceAlertDTO {
    private Long id;
    private String type; // FRAIS_ELEVES, DOSSIER_INACTIF, BUDGET_DEPASSE, ACTION_RISQUE
    private String message;
    private Long dossierId;
    private String dossierNumero;
    private String agentNom;
    private String niveau; // INFO, WARNING, DANGER
    private LocalDate dateDeclenchement;
}

