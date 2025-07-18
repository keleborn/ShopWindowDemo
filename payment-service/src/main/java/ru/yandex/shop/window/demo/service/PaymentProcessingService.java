package ru.yandex.shop.window.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.server.model.BalanceResponse;
import ru.yandex.shop.window.demo.server.model.PaymentRequest;
import ru.yandex.shop.window.demo.server.model.PaymentResponse;

import java.math.BigDecimal;

@Service
public class PaymentProcessingService {
    public Mono<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        return Mono.just(new PaymentResponse().success(true));
    }

    public Mono<BalanceResponse> getBalance(String name) {
        return Mono.just(new BalanceResponse().balance(BigDecimal.ZERO));
    }
}
