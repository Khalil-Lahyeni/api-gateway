package com.fleetmanagement.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    /**
     * Routes vers les micro-services avec TokenRelay filter.
     *
     * TokenRelay = filtre Spring Cloud Gateway qui :
     *   1. Récupère le JWT stocké en session Redis
     *   2. L'injecte automatiquement dans le header Authorization: Bearer <JWT>
     *   3. Transmet la requête au micro-service
     *
     * Angular n'envoie que le cookie SESSION — jamais le JWT directement.
     * C'est le Gateway qui résout session → JWT → micro-service.
     *
     * Exemple :
     *   Angular  →  GET /api/collecte/metrics  (+ cookie SESSION)
     *   Gateway  →  résout session → JWT
     *   Gateway  →  GET /metrics (+ Authorization: Bearer JWT)  →  ms-collecte:8081
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── MS Collecte / Ingestion ──
                .route("ms-collecte", route -> route
                        .path("/api/collecte/**")
                        .filters(filter -> filter
                                .stripPrefix(2)
                                .tokenRelay()   // injecte automatiquement le JWT
                        )
                        .uri("http://ms-collecte:8081")
                )

                // ── MS Alertes & Notifications ──
                .route("ms-alertes", route -> route
                        .path("/api/alertes/**")
                        .filters(filter -> filter
                                .stripPrefix(2)
                                .tokenRelay()
                        )
                        .uri("http://ms-alertes:8082")
                )

                // ── MS ML / Maintenance Prédictive ──
                .route("ms-ml", route -> route
                        .path("/api/ml/**")
                        .filters(filter -> filter
                                .stripPrefix(2)
                                .tokenRelay()
                        )
                        .uri("http://ms-ml:8083")
                )

                .build();
    }
}