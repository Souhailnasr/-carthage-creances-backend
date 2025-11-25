package projet.carthagecreance_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionAmiableRequestDTO {
    private BigDecimal montantRecouvre;
    private String reponseDebiteur; // POSITIVE ou NEGATIVE
}

