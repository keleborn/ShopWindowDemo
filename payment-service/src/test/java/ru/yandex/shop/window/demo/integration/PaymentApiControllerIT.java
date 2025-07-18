package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.shop.window.demo.server.model.PaymentRequest;
import ru.yandex.shop.window.demo.service.PaymentProcessingService;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureWebTestClient
public class PaymentApiControllerIT {
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private PaymentProcessingService paymentProcessingService;

    @Test
    void processPayment_shouldReturn200() {
        PaymentRequest paymentRequest = new PaymentRequest("test", BigDecimal.valueOf(100));

        webClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo("true");
    }

    @Test
    void processPayment_shouldReturn404WhenUserDoesNotExist() {
        PaymentRequest paymentRequest = new PaymentRequest("test_user", BigDecimal.valueOf(100));

        webClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void processPayment_shouldReturn400WhenBalanceIsNotEnough() {
        PaymentRequest paymentRequest = new PaymentRequest("test", BigDecimal.valueOf(100000));

        webClient.post()
                .uri("/api/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getBalance_shouldReturn200() {
        webClient.get()
                .uri("/api/payment/test")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(BigDecimal.valueOf(99999.99));
    }

    @Test
    void getBalance_shouldReturn400WhenServiceReturns404() {
        webClient.get()
                .uri("/api/payment/test_user")
                .exchange()
                .expectStatus().isNotFound();
    }
}
