package projet.carthagecreance_backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import projet.carthagecreance_backend.Entity.Utilisateur;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Service de détails utilisateur personnalisé pour Spring Security
 * 
 * Ce service charge les détails d'un utilisateur à partir de la base de données
 * et les convertit en format Spring Security.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurService utilisateurService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Utilisateur> utilisateurOpt = utilisateurService.getUtilisateurByEmail(email);
        
        if (!utilisateurOpt.isPresent()) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        
        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .authorities(getAuthorities(utilisateur))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Récupère les autorités (rôles) d'un utilisateur
     * 
     * @param utilisateur L'utilisateur
     * @return Collection des autorités
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Utilisateur utilisateur) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Ajouter le rôle principal
        authorities.add(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRoleUtilisateur().name()));
        
        // Ajouter des autorités supplémentaires si nécessaire
        switch (utilisateur.getRoleUtilisateur()) {
            case SUPER_ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case CHEF_DEPARTEMENT_DOSSIER:
            case CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE:
            case CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE:
            case CHEF_DEPARTEMENT_FINANCE:
                authorities.add(new SimpleGrantedAuthority("ROLE_CHEF"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            case AGENT_DOSSIER:
            case AGENT_RECOUVREMENT_AMIABLE:
            case AGENT_RECOUVREMENT_JURIDIQUE:
            case AGENT_FINANCE:
                authorities.add(new SimpleGrantedAuthority("ROLE_AGENT"));
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
            default:
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                break;
        }
        
        return authorities;
    }
}
