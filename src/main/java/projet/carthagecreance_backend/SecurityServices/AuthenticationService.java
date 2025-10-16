package projet.carthagecreance_backend.SecurityServices;


import lombok.RequiredArgsConstructor;

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
import projet.carthagecreance_backend.Service.NotificationService;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

	
	
    private final UtilisateurRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    public AuthenticationResponse register(RegisterRequest request) {

        var user = Utilisateur.builder()
                .nom(request.getFirstName())
                .prenom(request.getLastName())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getPassword()))
                .roleUtilisateur(request.getRole())
                .build();

        var savedUser = repository.save(user);


        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
 
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        if (user == null) {
            throw new UsernameNotFoundException(user +"Not Found");
        }		
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

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
