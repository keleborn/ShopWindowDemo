package ru.yandex.shop.window.demo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.client.api.PaymentApi;
import ru.yandex.shop.window.demo.client.model.BalanceResponse;
import ru.yandex.shop.window.demo.client.model.PaymentRequest;
import ru.yandex.shop.window.demo.client.model.PaymentResponse;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class PaymentService {
    private final PaymentApi paymentApi;

    public Mono<Boolean> processPayment(String userName, BigDecimal amount) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setUserName(userName);

        return paymentApi.processPayment(paymentRequest)
                .map(PaymentResponse::getSuccess);
    }

    public Mono<BigDecimal> getBalance(String userId) {
        return paymentApi.balanceUserIdGet(userId)
                .map(BalanceResponse::getBalance);
    }
}
