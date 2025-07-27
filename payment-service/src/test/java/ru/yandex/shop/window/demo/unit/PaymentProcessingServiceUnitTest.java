package ru.yandex.shop.window.demo.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.shop.window.demo.model.Account;
import ru.yandex.shop.window.demo.repository.AccountRepository;
import ru.yandex.shop.window.demo.server.model.PaymentRequest;
import ru.yandex.shop.window.demo.service.PaymentProcessingService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessingServiceUnitTest {
    private PaymentProcessingService paymentProcessingService;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        paymentProcessingService = new PaymentProcessingService(accountRepository);
    }

    @Test
    void processPayment_shouldReturnErrorWhenAccountDoesNotExist() {
        when(accountRepository.findByUserName("user")).thenReturn(Mono.empty());

        StepVerifier.create(paymentProcessingService.processPayment(new PaymentRequest("user", BigDecimal.valueOf(100))))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) e).getReason()).isEqualTo("Пользователь не найден");
                }).verify();
    }

    @Test
    void processPayment_shouldReturnErrorWhenBalanceIsNotEnough() {
        when(accountRepository.findByUserName("user")).thenReturn(Mono.just(new Account(1L, "user", BigDecimal.valueOf(100))));

        StepVerifier.create(paymentProcessingService.processPayment(new PaymentRequest("user", BigDecimal.valueOf(1000))))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) e).getReason()).isEqualTo("Недостаточно средств");
                }).verify();
    }

    @Test
    void processPayment_shouldProcessPaymentSuccessfully() {
        when(accountRepository.findByUserName("user")).thenReturn(Mono.just(new Account(1L, "user", BigDecimal.valueOf(100))));
        when(accountRepository.save(any())).thenReturn(Mono.just(new Account(1L, "user", BigDecimal.valueOf(1))));

        StepVerifier.create(paymentProcessingService.processPayment(new PaymentRequest("user", BigDecimal.valueOf(99))))
                .expectNextMatches(resp -> resp.getBody().getSuccess()).verifyComplete();
    }

    @Test
    void getBalance_shouldReturnErrorWhenAccountDoesNotExist() {
        when(accountRepository.findByUserName("user")).thenReturn(Mono.empty());

        StepVerifier.create(paymentProcessingService.getBalance("user"))
                .expectErrorSatisfies(e -> {
                    assertThat(e).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) e).getReason()).isEqualTo("Пользователь не найден");
                }).verify();
    }

    @Test
    void getBalance_shouldReturnBalance() {
        when(accountRepository.findByUserName("user")).thenReturn(Mono.just(new Account(1L, "user", BigDecimal.valueOf(100))));

        StepVerifier.create(paymentProcessingService.getBalance("user"))
                .expectNextMatches(resp -> resp.getBody().getBalance().equals(BigDecimal.valueOf(100))).verifyComplete();
    }
}
