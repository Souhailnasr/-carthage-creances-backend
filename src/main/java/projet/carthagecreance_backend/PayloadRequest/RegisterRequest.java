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

	// Getters et setters pour compatibilité
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	
	public RoleUtilisateur getRole() { return role; }
	public void setRole(RoleUtilisateur role) { this.role = role; }
}