package ru.yandex.shop.window.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.UsernameForm;
import ru.yandex.shop.window.demo.services.PaymentService;

@Controller
@RequestMapping("/balance")
public class BalanceController {
    private final PaymentService paymentService;

    public BalanceController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String balance() {
        return "balance";
    }

    @PostMapping
    public Mono<String> getBalance(@ModelAttribute UsernameForm usernameForm, Model model) {
        return paymentService.getBalance(usernameForm.getUsername())
                .map(balance -> {
                    model.addAttribute("balance", balance);
                    return "balance";
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.just("redirect:/balance/404");
                    }
                    if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.just("redirect:/balance/400");
                    }
                    return Mono.just("redirect:/balance/payment-error");
                });
    }

    @GetMapping("404")
    public String page404() {
        return "404";
    }

    @GetMapping("400")
    public String page400() {
        return "400";
    }

    @GetMapping("payment-error")
    public String errorPage() {
        return "payment-error";
    }
}
