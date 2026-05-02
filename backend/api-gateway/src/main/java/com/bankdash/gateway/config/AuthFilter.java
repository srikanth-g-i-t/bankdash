package com.bankdash.gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Base64;

@Component
@Slf4j
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthFilter() { super(Config.class); }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Extract the real UUID stored in the "userId" claim
                String username = claims.getSubject();
                String userIdClaim = claims.get("userId", String.class);

// Fallback: if userId claim missing use subject — must be final for lambda
                final String userId = (userIdClaim != null) ? userIdClaim : username;

                ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r
                                .header("X-User-Id",  userId)
                                .header("X-Username", username)
                        )
                        .build();

                return chain.filter(mutated);

            } catch (JwtException e) {
                log.warn("Invalid JWT: {}", e.getMessage());
                return onError(exchange, "Invalid or expired token");
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Key getKey() {
        byte[] bytes = Base64.getEncoder().encode(jwtSecret.getBytes());
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(new String(bytes)));
    }

    public static class Config {}
}
