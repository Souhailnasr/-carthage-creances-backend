package projet.carthagecreance_backend.PayloadRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import projet.carthagecreance_backend.Entity.RoleUtilisateur;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

	// @NotBlank(message = "First name is required")
	private String firstName;

	// @NotBlank(message = "Last name is required")
	private String lastName;

   // @Email(message = "Invalid email address")
   // @NotBlank(message = "Email is required")
    private String email;

	// @NotBlank(message = "Password is required")
	// @Size(min = 8, message = "Password must be at least 8 characters long")
	private String password;
	private RoleUtilisateur role;
}