package projet.carthagecreance_backend.Entity;

import org.springframework.security.core.GrantedAuthority;

public enum RoleUtilisateur implements GrantedAuthority {
    SUPER_ADMIN,
    CHEF_DEPARTEMENT_DOSSIER,
    CHEF_DEPARTEMENT_RECOUVREMENT_AMIABLE,
    CHEF_DEPARTEMENT_RECOUVREMENT_JURIDIQUE,
    CHEF_DEPARTEMENT_FINANCE,
    AGENT_DOSSIER,
    AGENT_RECOUVREMENT_AMIABLE,
    AGENT_RECOUVREMENT_JURIDIQUE,
    AGENT_FINANCE;

    @Override
    public String getAuthority() {
        return "RoleUtilisateur_" + name();
    }

}

