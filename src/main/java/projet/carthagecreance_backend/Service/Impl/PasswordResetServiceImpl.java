package projet.carthagecreance_backend.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projet.carthagecreance_backend.Entity.PasswordResetToken;
import projet.carthagecreance_backend.Entity.TokenStatut;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.Repository.PasswordResetTokenRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.Service.EmailService;
import projet.carthagecreance_backend.Service.PasswordResetService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // Rate limiting : 3 demandes par heure par email
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    
    @Override
    @Transactional
    public boolean generateResetToken(String email) {
        log.info("Génération d'un token de réinitialisation pour l'email: {}", email);
        
        // Vérifier que l'utilisateur existe
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);
        if (utilisateurOpt.isEmpty()) {
            // Ne pas révéler que l'email n'existe pas (sécurité)
            log.warn("Tentative de réinitialisation pour un email inexistant: {}", email);
            return false;
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        
        // Rate limiting : vérifier le nombre de demandes récentes
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = tokenRepository.countByUtilisateurEmailAndStatutAndDateCreationAfter(
            email, TokenStatut.ACTIF, oneHourAgo
        );
        
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Trop de demandes de réinitialisation pour l'email: {} (limite: {}/heure)", email, MAX_REQUESTS_PER_HOUR);
            return false;
        }
        
        // Invalider les tokens actifs existants
        invalidateActiveTokens(email);
        
        // Générer un nouveau token unique
        String token = UUID.randomUUID().toString();
        
        // Créer et sauvegarder le token
        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .utilisateur(utilisateur)
            .statut(TokenStatut.ACTIF)
            .build();
        
        // Les dates sont définies automatiquement par @PrePersist
        PasswordResetToken savedToken = tokenRepository.save(resetToken);
        if (savedToken == null) {
            log.error("Erreur lors de la sauvegarde du token de réinitialisation");
            return false;
        }
        
        // Envoyer l'email
        try {
            emailService.sendPasswordResetEmail(utilisateur.getEmail(), utilisateur.getNom(), token);
            log.info("Email de réinitialisation envoyé à: {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation: {}", e.getMessage(), e);
            // Ne pas échouer si l'email ne peut pas être envoyé (pour le développement)
            // En production, vous pourriez vouloir gérer cela différemment
        }
        
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        log.info("Validation du token de réinitialisation");
        
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.warn("Token non trouvé: {}", token);
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Vérifier que le token est actif
        if (resetToken.getStatut() != TokenStatut.ACTIF) {
            log.warn("Token non actif: {} (statut: {})", token, resetToken.getStatut());
            return false;
        }
        
        // Vérifier que le token n'est pas expiré
        if (resetToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            log.warn("Token expiré: {} (expiration: {})", token, resetToken.getDateExpiration());
            // Marquer comme expiré
            resetToken.setStatut(TokenStatut.EXPIRE);
            tokenRepository.save(resetToken);
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        log.info("Réinitialisation du mot de passe avec token");
        
        // Valider le token
        if (!validateToken(token)) {
            log.error("Token invalide pour la réinitialisation: {}", token);
            return false;
        }
        
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        Utilisateur utilisateur = resetToken.getUtilisateur();
        
        // Valider la force du mot de passe
        if (!isPasswordValid(newPassword)) {
            log.warn("Mot de passe invalide (force insuffisante)");
            return false;
        }
        
        // Hasher le nouveau mot de passe
        String hashedPassword = passwordEncoder.encode(newPassword);
        
        // Mettre à jour le mot de passe
        utilisateur.setMotDePasse(hashedPassword);
        utilisateurRepository.save(utilisateur);
        
        // Marquer le token comme utilisé
        resetToken.setStatut(TokenStatut.UTILISE);
        resetToken.setDateUtilisation(LocalDateTime.now());
        tokenRepository.save(resetToken);
        
        log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", utilisateur.getEmail());
        
        return true;
    }
    
    @Override
    @Transactional
    public void invalidateActiveTokens(String email) {
        log.info("Invalidation des tokens actifs pour l'email: {}", email);
        tokenRepository.invalidateActiveTokensByEmail(email);
    }
    
    /**
     * Valide la force du mot de passe
     * Règles : au moins 8 caractères, une majuscule, une minuscule, un chiffre, un caractère spécial
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
        
        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}

