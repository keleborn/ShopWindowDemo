package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
public abstract class AbstractAuthenticatedIT {
    @Autowired
    protected WebTestClient webClient;

    protected WebTestClient authenticatedClient;

    @BeforeEach
    void setUpAuthenticatedClient() {
        TestingAuthenticationToken mockAuth = new TestingAuthenticationToken("test", "password", "ROLE_USER");

        this.authenticatedClient = webClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuth))
                .mutate()
                .build();
    }

    protected String mockAccessToken() {
        FluxExchangeResult<Void> result = authenticatedClient.post()
                .uri("/__test__/inject-token")
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class);

        String sessionId = result.getResponseCookies()
                .getFirst("SESSION")
                .getValue();
        return sessionId;
    }
}
