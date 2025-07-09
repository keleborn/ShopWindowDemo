package ru.yandex.shop.window.demo.services;

import org.springframework.stereotype.Service;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Product;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {
    private final Map<Long, CartItem> cart = new HashMap<Long, CartItem>();

    public void addCartItem(Product product) {
        cart.compute(product.getId(), (id, item) -> {
            if (item == null) {
                return new CartItem(product);
            }
            item.setQuantity(item.getQuantity() + 1);
            return item;
        });
    }

    public void removeCartItem(Product product) {
        cart.remove(product.getId());
    }

    public Collection<CartItem> getCartItems() {
        return cart.values();
    }

    public BigDecimal getTotal() {
        return cart.values().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart() {
        cart.clear();
    }
}
