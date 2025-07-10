package ru.yandex.shop.window.demo.test.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {
    private CartService cartService;
    private Product product;
    private int quantity;

    @BeforeEach
    void setUp() {
        cartService = new CartService();
        product = new Product();
        product.setId(1L);
        product.setName("test");
        product.setPrice(BigDecimal.valueOf(100));
        product.setAvailable(true);

        quantity = 10;
    }

    @Test
    void addCartItem_shouldAddItemToCartAndIncreaseQuantity() {
        cartService.addCartItem(product, quantity);

        var items = cartService.getCartItems();
        assertEquals(1, items.size());
        assertEquals(quantity, items.iterator().next().getQuantity());
    }

    @Test
    void setQuantity_shouldUpdateQuantity() {
        int initialQuantity = 1;
        cartService.addCartItem(product, initialQuantity);
        cartService.setQuantity(product.getId(), quantity);

        assertEquals(quantity, cartService.getCartItems().iterator().next().getQuantity());
    }

    @Test
    void setQuantity_shouldRemoveItemOnZeroQuantity() {
        cartService.addCartItem(product, quantity);
        cartService.setQuantity(product.getId(), 0);

        assertEquals(0, cartService.getCartItems().size());
    }

    @Test
    void removeCartItem_shouldRemoveItemFromCart() {
        cartService.addCartItem(product, quantity);
        cartService.removeCartItem(product);

        assertEquals(0, cartService.getCartItems().size());
    }

    @Test
    void getTotal_shouldReturnTotal() {
        cartService.addCartItem(product, quantity);

        assertEquals(BigDecimal.valueOf(quantity).multiply(product.getPrice()), cartService.getTotal());
    }
}
