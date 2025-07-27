package ru.yandex.shop.window.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.UserForm;
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
    public Mono<String> getBalance(@ModelAttribute UserForm userForm, Model model, ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    String token = (String) session.getAttributes().get("access_token");
                    if (token == null) {
                        return Mono.error(new IllegalStateException("Пользователь не авторизован в Keycloak"));
                    }
                    return paymentService.getBalance(token, userForm.getUsername())
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
