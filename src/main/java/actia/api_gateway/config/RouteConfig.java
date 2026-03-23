package actia.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── Micro-service ──
                .route("Micro-service", route -> route
                        .path("/api/Micro-service/**")
                        .filters(filter -> filter
                                .stripPrefix(2)
                                .tokenRelay()   // injecte automatiquement le JWT
                        )
                        .uri("http://micro-service:8081")
                )
                .build();
    }
}