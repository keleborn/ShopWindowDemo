package ru.yandex.shop.window.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.CartForm;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
public class CartController {
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CartController(CartService cartService, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping("/cart")
    public Mono<String> showCart(Model model) {
        return Mono.fromSupplier(() -> {
            model.addAttribute("items", cartService.getCartItems());
            model.addAttribute("total", cartService.getTotal());
            return "cart";
        });
    }


    @PostMapping("/cart/update/{id}")
    public Mono<String> updateCart(@PathVariable Long id, CartForm form) {
        return Mono.fromRunnable(() -> cartService.setQuantity(id, form.getQuantity()))
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
    public Mono<String> processCheckout(@ModelAttribute("order") Order order) {
        List<OrderItem> orderItems = cartService.getCartItems()
                .stream()
                .map(CartItem::toOrderItem)
                .toList();

        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    Objects.requireNonNull(savedOrder.getId(), "Order id is null");
                    orderItems.forEach(orderItem -> orderItem.setOrderId(savedOrder.getId()));
                    return Flux.fromIterable(orderItems)
                            .flatMap(orderItemRepository::save)
                            .then(Mono.just(savedOrder));
                })
                .doOnNext(saved -> cartService.clearCart())
                .map(saved -> "redirect:/orders/" + saved.getId());
    }
}
