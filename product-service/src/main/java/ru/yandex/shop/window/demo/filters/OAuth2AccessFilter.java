package ru.yandex.shop.window.demo.filters;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

@Component
public class OAuth2AccessFilter implements WebFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/cart/checkout",
            "/balance"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (!PROTECTED_PATHS.contains(path)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    Authentication auth = context.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) {
                        return redirect("/login", exchange);
                    }

                    return exchange.getSession()
                            .flatMap(session -> {
                                boolean hasToken = session.getAttributes().containsKey("access_token");
                                if (!hasToken) {
                                    return redirect("/oauth2/keycloak", exchange);
                                }
                                return chain.filter(exchange);
                            });
                });
    }

    private Mono<Void> redirect(String location, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        exchange.getResponse().getHeaders().setLocation(URI.create(location));
        return exchange.getResponse().setComplete();
    }
}


