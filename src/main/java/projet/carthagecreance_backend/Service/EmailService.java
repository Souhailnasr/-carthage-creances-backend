package projet.carthagecreance_backend.Service;

public interface EmailService {
    
    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param email Email du destinataire
     * @param nom Nom du destinataire
     * @param token Token de réinitialisation
     */
    void sendPasswordResetEmail(String email, String nom, String token);
}

