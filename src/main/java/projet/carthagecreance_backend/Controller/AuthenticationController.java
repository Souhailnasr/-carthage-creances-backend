package projet.carthagecreance_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import projet.carthagecreance_backend.PayloadRequest.AuthenticationRequest;
import projet.carthagecreance_backend.PayloadRequest.RegisterRequest;
import projet.carthagecreance_backend.PayloadResponse.AuthenticationResponse;
import projet.carthagecreance_backend.SecurityServices.AuthenticationService;
import projet.carthagecreance_backend.SecurityServices.LogoutService;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Entity.Utilisateur;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
	private final AuthenticationService service;
	private final LogoutService logoutService;
	private final UtilisateurRepository utilisateurRepository;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request,
                                                           BindingResult result){
		if (result.hasErrors()) {
			List<String> errors = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.toList());
			return ResponseEntity.badRequest().body(AuthenticationResponse.builder().errors(errors).build());
		}
		return ResponseEntity.ok(service.register(request));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		System.out.println(request);
		return ResponseEntity.ok(service.authenticate(request));
	}

	/**
	 * Endpoint explicite pour le logout
	 * Met à jour derniere_deconnexion de l'utilisateur
	 * POST /auth/logout
	 * Headers: Authorization: Bearer {token}
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) {
		try {
			// Appeler le LogoutService qui met à jour derniere_deconnexion
			logoutService.logout(request, response, authentication);
			return ResponseEntity.ok(Map.of("message", "Logout successful"));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("error", "Erreur lors du logout: " + e.getMessage()));
		}
	}

	/**
	 * Endpoint de test pour forcer la mise à jour de derniere_deconnexion
	 * POST /auth/test-logout/{userId}
	 * Utilisez cet endpoint pour tester si la sauvegarde fonctionne
	 */
	@PostMapping("/test-logout/{userId}")
	public ResponseEntity<Map<String, Object>> testLogout(@PathVariable Long userId) {
		try {
			Utilisateur user = utilisateurRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
			
			LocalDateTime before = user.getDerniereDeconnexion();
			LocalDateTime now = LocalDateTime.now();
			
			user.setDerniereDeconnexion(now);
			Utilisateur saved = utilisateurRepository.save(user);
			
			// Vérifier en relisant
			Utilisateur verify = utilisateurRepository.findById(userId).orElse(null);
			
			return ResponseEntity.ok(Map.of(
					"message", "Test logout effectué",
					"userId", userId,
					"email", user.getEmail(),
					"avant", before != null ? before.toString() : "NULL",
					"apres_set", now.toString(),
					"apres_save", saved.getDerniereDeconnexion() != null ? saved.getDerniereDeconnexion().toString() : "NULL",
					"apres_verification", verify != null && verify.getDerniereDeconnexion() != null ? verify.getDerniereDeconnexion().toString() : "NULL"
			));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("error", "Erreur: " + e.getMessage()));
		}
	}
}
