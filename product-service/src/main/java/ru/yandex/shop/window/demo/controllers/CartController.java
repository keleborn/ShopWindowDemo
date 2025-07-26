package ru.yandex.shop.window.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.CartForm;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderDto;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.services.CartService;
import ru.yandex.shop.window.demo.services.OrderService;
import ru.yandex.shop.window.demo.services.PaymentService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CartController(CartService cartService, OrderService orderService, PaymentService paymentService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping("/cart")
    public Mono<String> showCart(Model model, Principal principal) {
        return Mono.fromSupplier(() -> {
            model.addAttribute("items", cartService.getCartItems(principal.getName()));
            model.addAttribute("total", cartService.getTotal(principal.getName()));
            return "cart";
        });
    }


    @PostMapping("/cart/update/{id}")
    public Mono<String> updateCart(@PathVariable Long id, CartForm form, Principal principal) {
        return Mono.fromRunnable(() -> cartService.setQuantity(principal.getName(), id, form.getQuantity()))
                .thenReturn("redirect:/cart");
    }

    @GetMapping("/cart/checkout")
    public Mono<String> checkout(Model model) {
        return Mono.fromSupplier(() -> {
            model.addAttribute("order", new Order());
            return "checkout";
        });
    }

    @PostMapping("/cart/checkout")
    public Mono<String> processCheckout(@ModelAttribute("order") Order order, Principal principal) {
        String username = principal.getName();
        List<OrderItem> orderItems = cartService.getCartItems(username)
                .stream()
                .map(CartItem::toOrderItem)
                .toList();

        order.setCreatedAt(LocalDateTime.now());

        return paymentService.processPayment(order.getCustomerName(), cartService.getTotal(username))
                .flatMap(success -> orderService.saveOrderWithItems(new OrderDto(order, orderItems))
                        .doOnNext(saved -> cartService.clearCart(username))
                        .map(saved -> "redirect:/orders/" + saved.getId()))
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
}
