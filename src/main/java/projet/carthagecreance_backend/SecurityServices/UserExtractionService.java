package projet.carthagecreance_backend.SecurityServices;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Entity.Utilisateur;

import javax.crypto.SecretKey;
import java.util.Optional;

@Service
public class UserExtractionService {

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
     */
    public Long extractUserIdFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
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

            return null;
        } catch (Exception e) {
            System.out.println("Erreur lors de l'extraction de l'ID utilisateur: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrait l'utilisateur complet depuis le token JWT
     * @param token Le token JWT
     * @return L'utilisateur ou null si le token est invalide
     */
    public Utilisateur extractUserFromToken(String token) {
        try {
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return null;
            }

            Optional<Utilisateur> utilisateur = utilisateurRepository.findById(userId);
            return utilisateur.orElse(null);
        } catch (Exception e) {
            System.out.println("Erreur lors de l'extraction de l'utilisateur: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrait l'email utilisateur depuis le token JWT
     * @param token Le token JWT
     * @return L'email de l'utilisateur ou null si le token est invalide
     */
    public String extractEmailFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
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
        } catch (Exception e) {
            System.out.println("Erreur lors de l'extraction de l'email: " + e.getMessage());
            return null;
        }
    }
}

