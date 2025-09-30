package projet.carthagecreance_backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // désactive CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Active CORS
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // tout est accessible
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4200", "http://localhost:3000", "http://127.0.0.1:4200", "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
            "Access-Control-Request-Method", "Access-Control-Request-Headers", "Cache-Control", "Pragma"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials", "Access-Control-Allow-Headers"
        ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
