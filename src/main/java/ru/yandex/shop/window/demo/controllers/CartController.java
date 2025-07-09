package ru.yandex.shop.window.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.shop.window.demo.services.CartService;

@Controller
public class CartController {
    private final CartService cartService;

    public CartController(final CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        return "cart";
    }


    @PostMapping("/cart/update/{id}")
    public String updateCart(@PathVariable Long id, @RequestParam int quantity, Model model) {
        cartService.setQuantity(id, quantity);
        return "redirect:/cart";
    }
}
