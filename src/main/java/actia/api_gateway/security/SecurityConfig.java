package com.fleetmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /**
     * Flux d'authentification (d'après les diagrammes) :
     *
     * 1. Angular appelle /api/leads sans session
     * 2. Gateway redirige → Keycloak (302)
     * 3. Utilisateur se connecte sur Keycloak
     * 4. Keycloak redirige → /callback?code=xxx
     * 5. Gateway échange le code contre un JWT
     * 6. Gateway stocke le JWT en session Redis
     * 7. Gateway renvoie un cookie SESSION (HTTP-only) à Angular
     * 8. Toutes les prochaines requêtes Angular utilisent ce cookie
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                        // Endpoints publics
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Callback Keycloak — doit être accessible sans session
                        .pathMatchers("/login/oauth2/code/**").permitAll()
                        // Tout le reste nécessite une session valide
                        .anyExchange().authenticated()
                )

                // ── OAuth2 Login (Authorization Code Flow) ──
                // Quand une requête non authentifiée arrive,
                // Spring redirige automatiquement vers Keycloak
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(
                                // Utilise le client "keycloak" défini dans application.yml
                                null // Spring utilise le resolver par défaut
                        )
                )

                // ── Logout ──
                // Déconnexion côté Keycloak + suppression session Redis
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                );

        return http.build();
    }

    /**
     * Handler de logout OIDC :
     * Redirige vers Keycloak pour invalider la session SSO
     * puis redirige vers l'accueil Angular
     */
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler handler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri("http://localhost:4200");
        return handler;
    }
}