package ru.yandex.shop.window.demo.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.server.api.PaymentApi;
import ru.yandex.shop.window.demo.server.model.BalanceResponse;
import ru.yandex.shop.window.demo.server.model.PaymentRequest;
import ru.yandex.shop.window.demo.server.model.PaymentResponse;
import ru.yandex.shop.window.demo.service.PaymentProcessingService;

@RestController
public class PaymentApiController implements PaymentApi {
    private final PaymentProcessingService paymentService;

    public PaymentApiController(PaymentProcessingService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest.flatMap(paymentService::processPayment);
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String username, ServerWebExchange exchange) {
        return paymentService.getBalance(username);
    }
}
