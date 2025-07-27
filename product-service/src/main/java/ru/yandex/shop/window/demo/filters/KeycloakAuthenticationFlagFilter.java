package ru.yandex.shop.window.demo.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class KeycloakAuthenticationFlagFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exchange.getSession()
                .map(session -> session.getAttributes().containsKey("access_token"))
                .flatMap(isAuthorized -> {
                    exchange.getAttributes().put("isKeycloakAuthorized", isAuthorized);
                    return chain.filter(exchange);
                });
    }
}

