package projet.carthagecreance_backend.SecurityServices;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import projet.carthagecreance_backend.Repository.TokenRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Entity.Utilisateur;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
	private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);
	
	private final TokenRepository tokenRepository;
	private final UtilisateurRepository utilisateurRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		logger.info("=== DÉBUT LOGOUT ===");
		
		final String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			logger.warn("Logout: Pas de header Authorization ou format invalide");
			return;
		}
		
		final String jwt = authHeader.substring(7);
		logger.info("Logout: Token JWT extrait (longueur: {})", jwt.length());
		
		var storedToken = tokenRepository.findByToken(jwt).orElse(null);
		if (storedToken == null) {
			logger.warn("Logout: Token non trouvé dans la base de données");
			return;
		}
		
		logger.info("Logout: Token trouvé, ID: {}", storedToken.getTokenId());
		
		// Récupérer l'utilisateur AVANT de révoquer le token
		var user = storedToken.getUser();
		if (user == null) {
			logger.warn("Logout: Utilisateur non trouvé pour le token");
			// Révoquer quand même le token
			storedToken.setExpired(true);
			storedToken.setRevoked(true);
			tokenRepository.save(storedToken);
			return;
		}
		
		logger.info("Logout: Utilisateur trouvé - ID: {}, Email: {}", user.getId(), user.getEmail());
		
		// Mettre à jour la date de déconnexion
		LocalDateTime now = LocalDateTime.now();
		logger.info("Logout: Avant mise à jour - derniere_deconnexion actuelle: {}", user.getDerniereDeconnexion());
		
		user.setDerniereDeconnexion(now);
		logger.info("Logout: Après setDerniereDeconnexion - valeur: {}", user.getDerniereDeconnexion());
		
		// Utiliser saveAndFlush pour forcer l'écriture immédiate en base
		Utilisateur savedUser = utilisateurRepository.saveAndFlush(user);
		logger.info("Logout: Utilisateur sauvegardé (saveAndFlush) - ID: {}, derniere_deconnexion: {}", 
				savedUser.getId(), savedUser.getDerniereDeconnexion());
		
		// Vérifier en relisant depuis la base (dans une nouvelle transaction)
		Utilisateur verifyUser = utilisateurRepository.findById(user.getId()).orElse(null);
		if (verifyUser != null) {
			logger.info("Logout: Vérification DB - derniere_deconnexion dans DB: {}", verifyUser.getDerniereDeconnexion());
			if (verifyUser.getDerniereDeconnexion() == null) {
				logger.error("Logout: ⚠️ ATTENTION - La valeur est NULL après sauvegarde! Problème de transaction ou de mapping.");
			} else {
				logger.info("Logout: ✅ SUCCÈS - derniere_deconnexion correctement sauvegardée: {}", verifyUser.getDerniereDeconnexion());
			}
		} else {
			logger.error("Logout: ERREUR - Utilisateur non trouvé après sauvegarde!");
		}
		
		logger.info("Logout: derniere_deconnexion mise à jour pour l'utilisateur {}: {}", user.getId(), now);
		
		// Révoquer le token
		storedToken.setExpired(true);
		storedToken.setRevoked(true);
		tokenRepository.save(storedToken);
		logger.info("Logout: Token révoqué");
		
		SecurityContextHolder.clearContext();
		logger.info("=== FIN LOGOUT ===");
	}
}
