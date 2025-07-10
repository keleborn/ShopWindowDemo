package ru.yandex.shop.window.demo.controllers;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.repository.OrderRepository;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepository;

    public OrderController(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String getOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrder(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        return "order";
    }
}
