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

    public void addCartItem(Product product, int quantity) {
//        cart.compute(product.getId(), (id, item) -> {
//            if (item == null) {
//                return new CartItem(product, quantity);
//            }
//            item.setQuantity(item.getQuantity() + quantity);
//            return item;
//        });
    }

    public void removeCartItem(Product product) {
        cart.remove(product.getId());
    }

    public Collection<CartItem> getCartItems() {
        return cart.values();
    }

    public BigDecimal getTotal() {
        return BigDecimal.ZERO;//.values().stream()
//                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clearCart() {
        cart.clear();
    }

    public void setQuantity(Long id, int quantity) {
//        if (quantity <= 0) {
//            cart.remove(id);
//        } else {
//            cart.computeIfPresent(id, (productId, item) -> {
//                item.setQuantity(quantity);
//                return item;
//            });
//        }
    }
}
