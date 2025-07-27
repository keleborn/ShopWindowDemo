package ru.yandex.shop.window.demo.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.UserForm;

@Controller
public class KeycloakController {
    private final WebClient keycloakClient;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String url;

    public KeycloakController(WebClient.Builder builder) {
        this.keycloakClient = builder
                .baseUrl(url)
                .build();
    }

    @GetMapping("/oauth2/keycloak")
    public String showPasswordForm() {
        return "oauth";
    }

    @PostMapping("/oauth2/keycloak")
    public Mono<String> authorizeKeycloak(@ModelAttribute UserForm userForm, ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> keycloakClient.post()
                        .uri(url + "/protocol/openid-connect/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("grant_type", "password")
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret)
                                .with("username", userForm.getUsername())
                                .with("password", userForm.getPassword()))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(json -> json.get("access_token").asText())
                        .doOnNext(token -> session.getAttributes().put("access_token", token))
                        .thenReturn("redirect:/products"));
    }
}

