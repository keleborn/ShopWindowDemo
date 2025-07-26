package ru.yandex.shop.window.demo.unit;

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
    private String username;
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

        username = "test";
        quantity = 10;
    }

    @Test
    void addCartItem_shouldAddItemToCartAndIncreaseQuantity() {
        cartService.addCartItem(username, product, quantity);

        var items = cartService.getCartItems(username);
        assertEquals(1, items.size());
        assertEquals(quantity, items.iterator().next().getQuantity());
    }

    @Test
    void setQuantity_shouldUpdateQuantity() {
        int initialQuantity = 1;
        cartService.addCartItem(username, product, initialQuantity);
        cartService.setQuantity(username, product.getId(), quantity);

        assertEquals(quantity, cartService.getCartItems(username).iterator().next().getQuantity());
    }

    @Test
    void setQuantity_shouldRemoveItemOnZeroQuantity() {
        cartService.addCartItem(username, product, quantity);
        cartService.setQuantity(username, product.getId(), 0);

        assertEquals(0, cartService.getCartItems(username).size());
    }

    @Test
    void removeCartItem_shouldRemoveItemFromCart() {
        cartService.addCartItem(username, product, quantity);
        cartService.removeCartItem(username, product);

        assertEquals(0, cartService.getCartItems(username).size());
    }

    @Test
    void getTotal_shouldReturnTotal() {
        cartService.addCartItem(username, product, quantity);

        assertEquals(BigDecimal.valueOf(quantity).multiply(product.getPrice()), cartService.getTotal(username));
    }

    @Test
    void clearCart_shouldClearCart() {
        cartService.addCartItem(username, product, quantity);

        cartService.clearCart(username);
        assertEquals(0, cartService.getCartItems(username).size());
    }

    @Test
    void clearCart_shouldNotClearCartForOtherUsers() {
        cartService.addCartItem(username, product, quantity);
        cartService.addCartItem("John", product, quantity);

        cartService.clearCart(username);
        assertEquals(1, cartService.getCartItems("John").size());
    }

    @Test
    void userCart_isIsolated() {
        cartService.addCartItem(username, product, quantity);
        cartService.addCartItem("John", product, quantity);

        assertEquals(1, cartService.getCartItems("John").size());
        assertEquals(quantity, cartService.getCartItems("John").iterator().next().getQuantity());
        assertEquals(1, cartService.getCartItems(username).size());
    }
}
