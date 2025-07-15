package ru.yandex.shop.window.demo.controllers;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.services.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Mono<String> getOrders(Model model) {
        return orderService.findAllWithItems()
                .doOnNext(orderDtos -> model.addAttribute("orders", orderDtos))
                .thenReturn("orders");
    }

    @GetMapping("/{id}")
    public Mono<String> getOrder(@PathVariable Long id, Model model) {
        return orderService.getOrderWithItems(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .doOnNext(orderDto -> model.addAttribute("order", orderDto))
                .thenReturn("order");
    }
}
