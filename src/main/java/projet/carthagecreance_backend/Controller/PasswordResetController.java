package projet.carthagecreance_backend.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projet.carthagecreance_backend.DTO.ForgotPasswordRequest;
import projet.carthagecreance_backend.DTO.ResetPasswordRequest;
import projet.carthagecreance_backend.Service.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    /**
     * Endpoint pour demander une réinitialisation de mot de passe
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Demande de réinitialisation de mot de passe pour l'email: {}", request.getEmail());
            
            // Générer le token et envoyer l'email
            passwordResetService.generateResetToken(request.getEmail());
            
            // Toujours retourner la même réponse (sécurité : ne pas révéler si l'email existe)
            return ResponseEntity.ok(Map.of(
                "message", "Si cet email existe, un lien de réinitialisation vous a été envoyé",
                "success", true
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "message", "Si cet email existe, un lien de réinitialisation vous a été envoyé",
                "success", true
            ));
        }
    }
    
    /**
     * Endpoint pour valider un token de réinitialisation
     * GET /api/auth/reset-password/validate?token={token}
     */
    @GetMapping("/reset-password/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        try {
            log.info("Validation du token de réinitialisation");
            
            boolean isValid = passwordResetService.validateToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "Token valide"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Token invalide ou expiré",
                    "error", "TOKEN_INVALID"
                ));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Token invalide ou expiré",
                "error", "TOKEN_INVALID"
            ));
        }
    }
    
    /**
     * Endpoint pour réinitialiser le mot de passe
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("Réinitialisation du mot de passe");
            
            // Valider que les deux mots de passe correspondent
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Les mots de passe ne correspondent pas",
                    "error", "PASSWORDS_MISMATCH"
                ));
            }
            
            // Réinitialiser le mot de passe
            boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Mot de passe réinitialisé avec succès",
                    "success", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Token invalide ou expiré, ou mot de passe invalide",
                    "error", "RESET_FAILED"
                ));
            }
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Erreur lors de la réinitialisation du mot de passe",
                "error", "RESET_FAILED"
            ));
        }
    }
    
    /**
     * Endpoint pour renvoyer un email de réinitialisation
     * POST /api/auth/forgot-password/resend
     */
    @PostMapping("/forgot-password/resend")
    public ResponseEntity<Map<String, Object>> resendResetEmail(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Renvoyer un email de réinitialisation pour l'email: {}", request.getEmail());
            
            // Invalider les tokens actifs existants
            passwordResetService.invalidateActiveTokens(request.getEmail());
            
            // Générer un nouveau token
            passwordResetService.generateResetToken(request.getEmail());
            
            // Toujours retourner la même réponse (sécurité)
            return ResponseEntity.ok(Map.of(
                "message", "Si cet email existe, un nouveau lien de réinitialisation vous a été envoyé",
                "success", true
            ));
        } catch (Exception e) {
            log.error("Erreur lors du renvoi de l'email: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "message", "Si cet email existe, un nouveau lien de réinitialisation vous a été envoyé",
                "success", true
            ));
        }
    }
}

