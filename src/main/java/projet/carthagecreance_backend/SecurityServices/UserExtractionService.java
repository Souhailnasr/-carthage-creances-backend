package projet.carthagecreance_backend.SecurityServices;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Entity.Utilisateur;

import javax.crypto.SecretKey;
import java.util.Optional;

@Service
public class UserExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(UserExtractionService.class);

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private final UtilisateurRepository utilisateurRepository;

    public UserExtractionService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Extrait l'ID utilisateur depuis le token JWT
     * @param token Le token JWT
     * @return L'ID de l'utilisateur ou null si le token est invalide
     * @throws ExpiredJwtException si le token est expiré
     */
    public Long extractUserIdFromToken(String token) throws ExpiredJwtException {
        try {
            if (token == null || token.isEmpty()) {
                logger.warn("Token JWT est null ou vide");
                return null;
            }

            // Supprimer le préfixe "Bearer " si présent
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extraire l'ID utilisateur depuis les claims
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            }

            logger.warn("Claim 'userId' non trouvé ou de type inattendu dans le token");
            return null;
        } catch (ExpiredJwtException e) {
            // Relancer l'exception pour qu'elle soit gérée explicitement par les contrôleurs
            logger.error("Token JWT expiré: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'ID utilisateur: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrait l'utilisateur complet depuis le token JWT
     * @param token Le token JWT
     * @return L'utilisateur ou null si le token est invalide
     * @throws ExpiredJwtException si le token est expiré
     */
    public Utilisateur extractUserFromToken(String token) throws ExpiredJwtException {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                logger.warn("Impossible d'extraire l'ID utilisateur du token");
                return null;
            }

            Optional<Utilisateur> utilisateur = utilisateurRepository.findById(userId);
            if (utilisateur.isPresent()) {
                logger.debug("Utilisateur extrait du token: ID={}, Email={}", userId, utilisateur.get().getEmail());
                return utilisateur.get();
            } else {
                logger.warn("Utilisateur avec ID {} non trouvé en base de données", userId);
                return null;
            }
        } catch (ExpiredJwtException e) {
            // Relancer l'exception pour qu'elle soit gérée explicitement par les contrôleurs
            logger.error("Token JWT expiré lors de l'extraction de l'utilisateur: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'utilisateur: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrait l'email utilisateur depuis le token JWT
     * @param token Le token JWT
     * @return L'email de l'utilisateur ou null si le token est invalide
     * @throws ExpiredJwtException si le token est expiré
     */
    public String extractEmailFromToken(String token) throws ExpiredJwtException {
        try {
            if (token == null || token.isEmpty()) {
                logger.warn("Token JWT est null ou vide lors de l'extraction de l'email");
                return null;
            }

            // Supprimer le préfixe "Bearer " si présent
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // Relancer l'exception pour qu'elle soit gérée explicitement par les contrôleurs
            logger.error("Token JWT expiré lors de l'extraction de l'email: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'email: {}", e.getMessage(), e);
            return null;
        }
    }
}

