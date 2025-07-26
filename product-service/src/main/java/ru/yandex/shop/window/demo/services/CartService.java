package ru.yandex.shop.window.demo.services;

import org.springframework.stereotype.Service;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Product;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {
    private final Map<String, Map<Long, CartItem>> carts = new ConcurrentHashMap<>();

    public void addCartItem(String username, Product product, int quantity) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, u -> new HashMap<>());
        cart.compute(product.getId(), (id, item) -> {
            if (item == null) {
                return new CartItem(product, quantity);
            }
            item.setQuantity(item.getQuantity() + quantity);
            return item;
        });
    }

    public void removeCartItem(String username, Product product) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, u -> new HashMap<>());
        cart.remove(product.getId());
    }

    public Collection<CartItem> getCartItems(String username) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, u -> new HashMap<>());
        return cart.values();
    }

    public BigDecimal getTotal(String username) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, u -> new HashMap<>());
        return cart.values().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart(String username) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, u -> new HashMap<>());
        cart.clear();
    }

    public void setQuantity(String username, Long id, int quantity) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, u -> new HashMap<>());
        if (quantity <= 0) {
            cart.remove(id);
        } else {
            cart.computeIfPresent(id, (productId, item) -> {
                item.setQuantity(quantity);
                return item;
            });
        }
    }
}
