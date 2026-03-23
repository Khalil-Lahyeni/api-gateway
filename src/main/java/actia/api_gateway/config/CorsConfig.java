package actia.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    /**
     * Configuration CORS globale :
     * Autorise Angular (localhost:4200) à appeler le Gateway
     * avec des requêtes authentifiées (allowCredentials = true)
     */
    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration corsConfig = new CorsConfiguration();

        // Origines autorisées
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:4200"   // Angular en dev
        ));

        // Méthodes HTTP autorisées
        corsConfig.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Headers autorisés
        corsConfig.setAllowedHeaders(List.of("*"));

        // Nécessaire pour envoyer les cookies / tokens
        corsConfig.setAllowCredentials(true);

        // Appliquer sur toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}