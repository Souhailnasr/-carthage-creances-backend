package projet.carthagecreance_backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Web MVC pour s'assurer que les routes /api/** sont toujours
 * gérées par le dispatcher servlet et non par le gestionnaire de ressources statiques
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // S'assurer que les routes /api/** ne sont pas traitées comme des ressources statiques
        // En ne configurant pas de gestionnaire pour /api/**, Spring utilisera le dispatcher servlet
        registry.setOrder(1);
    }
}

