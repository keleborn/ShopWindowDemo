package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void withoutToken_should401() {
        webTestClient.get().uri("/api/payment/John")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void withInvalidToken_should401() {
        webTestClient.get().uri("/api/payment/John")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void withMockJwt_should200() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .jwt(jwt -> jwt
                                .claim("preferred_username", "John")
                                .claim("scope", "openid profile payment:read")
                                .issuer("http://localhost:8083/realms/shop")
                        )
                )
                .get().uri("/api/payment/John")
                .exchange()
                .expectStatus().isOk();
    }
}
