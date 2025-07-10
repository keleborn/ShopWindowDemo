package ru.yandex.shop.window.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CartController {
    private final CartService cartService;
    private final OrderRepository orderRepository;

    public CartController(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        return "cart";
    }


    @PostMapping("/cart/update/{id}")
    public String updateCart(@PathVariable Long id, @RequestParam int quantity) {
        cartService.setQuantity(id, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/cart/checkout")
    public String checkout(Model model) {
        model.addAttribute("order", new Order());
        return "checkout";
    }

    @PostMapping("/cart/checkout")
    public String processCheckout(@ModelAttribute("order") Order order) {
        List<OrderItem> orderItems = cartService.getCartItems().stream()
                .map(CartItem::toOrderItem).toList();
        
        order.setOrderItems(orderItems);
        order.setCreatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();
        return "redirect:/orders/" + savedOrder.getId();
    }
}
