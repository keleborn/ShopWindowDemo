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

import java.math.BigDecimal;

@RestController
public class PaymentApiController implements PaymentApi {
    private final PaymentProcessingService paymentService;

    public PaymentApiController(PaymentProcessingService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new PaymentResponse().success(true)));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String username, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new BalanceResponse().balance(BigDecimal.ZERO).userId(username)));
    }
}
