package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
public abstract class AbstractAuthenticatedIT {
    @Autowired
    protected WebTestClient webClient;

    protected WebTestClient authenticatedClient;

    @BeforeEach
    void setUpAuthenticatedClient() {
        TestingAuthenticationToken mockAuth = new TestingAuthenticationToken("user", "password", "ROLE_USER");
        this.authenticatedClient = webClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(mockAuth));
    }
}
