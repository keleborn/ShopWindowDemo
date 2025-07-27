package ru.yandex.shop.window.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.repository.AccountRepository;
import ru.yandex.shop.window.demo.server.model.BalanceResponse;
import ru.yandex.shop.window.demo.server.model.PaymentRequest;
import ru.yandex.shop.window.demo.server.model.PaymentResponse;

import java.math.BigDecimal;

@Service
public class PaymentProcessingService {
    private final AccountRepository accountRepository;

    public PaymentProcessingService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Mono<ResponseEntity<PaymentResponse>> processPayment(PaymentRequest paymentRequest) {
        return accountRepository.findByUserName(paymentRequest.getUserName())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")))
                .flatMap(account -> {
                    BigDecimal subtractedBalance = account.getBalance().subtract(paymentRequest.getAmount());
                    if (subtractedBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно средств"));
                    }
                    account.setBalance(subtractedBalance);
                    return accountRepository.save(account)
                            .thenReturn(ResponseEntity.ok(new PaymentResponse().success(true).updatedBalance(subtractedBalance)));
                });
    }

    public Mono<ResponseEntity<BalanceResponse>> getBalance(String name) {
        return accountRepository.findByUserName(name)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")))
                .map(account -> ResponseEntity.ok(new BalanceResponse().balance(account.getBalance())));
    }
}
