package projet.carthagecreance_backend.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration Jackson pour la sérialisation des entités Hibernate
 * 
 * Cette configuration gère les proxies Hibernate et évite les erreurs de sérialisation
 * avec les relations lazy loading et les types de date Java 8.
 * 
 * @author Système de Gestion de Créances
 * @version 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * Configuration de l'ObjectMapper pour gérer les proxies Hibernate et les types Java 8
     * 
     * @return ObjectMapper configuré pour Hibernate et Java 8
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configuration générale pour éviter les erreurs avec les proxies Hibernate
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Configuration pour les types de date Java 8 (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());
        
        return mapper;
    }
}
