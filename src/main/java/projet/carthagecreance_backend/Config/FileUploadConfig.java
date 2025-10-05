package projet.carthagecreance_backend.Config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * Configuration pour la gestion des uploads de fichiers
 * 
 * Cette classe configure :
 * - Le MultipartResolver pour gérer les fichiers multipart
 * - Les limites de taille des fichiers
 * - Le support multipart pour Spring Boot
 */
@Configuration
public class FileUploadConfig {

    /**
     * Configuration des paramètres multipart
     * 
     * @return MultipartConfigElement avec les limites de taille
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Limite de taille pour un fichier individuel (10MB)
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        
        // Limite de taille totale pour la requête (20MB)
        factory.setMaxRequestSize(DataSize.ofMegabytes(20));
        
        // Seuil en mémoire avant écriture sur disque (2MB)
        factory.setFileSizeThreshold(DataSize.ofMegabytes(2));
        
        // Dossier temporaire pour les fichiers (optionnel)
        // factory.setLocation("/tmp");
        
        return factory.createMultipartConfig();
    }

    /**
     * Bean MultipartResolver pour gérer les fichiers multipart
     * 
     * Utilise StandardServletMultipartResolver qui est l'implémentation
     * recommandée pour Spring Boot avec Servlet 3.0+
     * 
     * @return MultipartResolver configuré
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        
        // Activer le support multipart
        resolver.setResolveLazily(true);
        
        return resolver;
    }
}
