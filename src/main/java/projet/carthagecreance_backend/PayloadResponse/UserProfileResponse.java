package projet.carthagecreance_backend.PayloadResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Long userId; // Changé de 'id' à 'userId' pour cohérence avec AuthenticationResponse
    private String nom;
    private String prenom;
    private String email;
    private String role;
}



