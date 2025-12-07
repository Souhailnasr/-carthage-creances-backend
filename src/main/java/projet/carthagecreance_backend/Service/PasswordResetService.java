package projet.carthagecreance_backend.Service;

public interface PasswordResetService {
    
    /**
     * Génère un token de réinitialisation et envoie un email
     * @param email Email de l'utilisateur
     * @return true si l'email existe et le token a été généré
     */
    boolean generateResetToken(String email);
    
    /**
     * Valide un token de réinitialisation
     * @param token Token à valider
     * @return true si le token est valide
     */
    boolean validateToken(String token);
    
    /**
     * Réinitialise le mot de passe avec un token
     * @param token Token de réinitialisation
     * @param newPassword Nouveau mot de passe
     * @return true si la réinitialisation a réussi
     */
    boolean resetPassword(String token, String newPassword);
    
    /**
     * Invalide tous les tokens actifs d'un utilisateur
     * @param email Email de l'utilisateur
     */
    void invalidateActiveTokens(String email);
}

