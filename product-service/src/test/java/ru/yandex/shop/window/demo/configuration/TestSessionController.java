package ru.yandex.shop.window.demo.configuration;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class TestSessionController {

    @PostMapping("/__test__/inject-token")
    public Mono<Void> injectAccessToken(ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().put("access_token", "mock-token"))
                .then();
    }
}

