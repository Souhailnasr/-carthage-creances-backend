package projet.carthagecreance_backend.SecurityServices;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Token;
import projet.carthagecreance_backend.Entity.TokenType;
import projet.carthagecreance_backend.Entity.Utilisateur;
import projet.carthagecreance_backend.PayloadRequest.AuthenticationRequest;
import projet.carthagecreance_backend.PayloadRequest.RegisterRequest;
import projet.carthagecreance_backend.PayloadResponse.AuthenticationResponse;
import projet.carthagecreance_backend.Repository.TokenRepository;
import projet.carthagecreance_backend.Repository.UtilisateurRepository;
import projet.carthagecreance_backend.SecurityConfig.JwtService;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
	
	
    private final UtilisateurRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {

        var user = Utilisateur.builder()
                .nom(request.getFirstName())
                .prenom(request.getLastName())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getPassword()))
                .roleUtilisateur(request.getRole())
                .actif(false) // Forcer actif = false pour les nouveaux utilisateurs
                .build();

        var savedUser = repository.save(user);


        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .nom(savedUser.getNom())
                .prenom(savedUser.getPrenom())
                .role(savedUser.getRoleUtilisateur().name())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        logger.info("=== DÉBUT AUTHENTICATION ===");
        logger.info("Email: {}", request.getEmail());
 
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + request.getEmail()));
        
        logger.info("Utilisateur trouvé: ID={}, Nom={}, Email={}, Role={}", 
                   user.getId(), user.getNom(), user.getEmail(), user.getRoleUtilisateur());
        
        user.setDerniereConnexion(LocalDateTime.now());
        user.setDerniereDeconnexion(null);
        // Mettre à jour le statut actif : utilisateur connecté = actif
        user.mettreAJourStatutActif();
        var userWithAudit = repository.save(user);

        var jwtToken = jwtService.generateToken(userWithAudit);
        revokeAllUserTokens(userWithAudit);
        saveUserToken(userWithAudit, jwtToken);
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(jwtToken)
                .userId(userWithAudit.getId())
                .email(userWithAudit.getEmail())
                .nom(userWithAudit.getNom())
                .prenom(userWithAudit.getPrenom())
                .role(userWithAudit.getRoleUtilisateur().name())
                .build();
        
        logger.info("Response générée: userId={}, email={}, nom={}, prenom={}, role={}", 
                   response.getUserId(), response.getEmail(), response.getNom(), 
                   response.getPrenom(), response.getRole());
        logger.info("=== FIN AUTHENTICATION ===");
        
        return response;
    }

    private void saveUserToken(Utilisateur user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Utilisateur user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
