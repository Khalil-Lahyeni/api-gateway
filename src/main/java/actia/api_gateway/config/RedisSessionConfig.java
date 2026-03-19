package com.fleetmanagement.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Configuration
@EnableRedisWebSession(
        maxInactiveIntervalInSeconds = 3600  // session expire après 1h d'inactivité
)
public class RedisSessionConfig {

    /**
     * Stockage des sessions en Redis.
     *
     * Sans cette config :
     *   - Les sessions sont en mémoire → perdues au redémarrage du Gateway
     *
     * Avec Redis :
     *   - Les sessions persistent après redémarrage ✅
     *   - Plusieurs instances du Gateway partagent les mêmes sessions ✅
     *     (utile si on scale horizontalement plus tard)
     *
     * Contenu d'une session Redis :
     *   SESSION:abc123 → {
     *     access_token:  "eyJhbGci...",
     *     refresh_token: "eyJhbGci...",
     *     expires_at:    1234567890,
     *     user_info:     { sub, email, roles... }
     *   }
     *
     * La connexion Redis est configurée dans application.yml
     * (spring.data.redis.host / port)
     */
}