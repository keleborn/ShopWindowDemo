package ru.yandex.shop.window.demo.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CartControllerIT {

    private Product product;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    public void setUp() throws Exception {
        productRepository.deleteAll();
        orderRepository.deleteAll();
        cartService.clearCart();
        product = productRepository.save(new Product("test", "desc", BigDecimal.valueOf(100), true));
        cartService.addCartItem(product, 2);
    }

    @Test
    void showCart_shouldShowCart() throws Exception {
        mvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void updateCart_shouldUpdateCart() throws Exception {
        mvc.perform(post("/cart/update/{id}", product.getId())
                        .param("quantity", "10"))
                .andExpect(status().is3xxRedirection());

        assertThat(cartService.getCartItems().iterator().next().getQuantity()).isEqualTo(10);
    }

    @Test
    void checkout_shouldShowCheckout() throws Exception {
        mvc.perform(get("/cart/checkout"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(view().name("checkout"));
    }

    @Test
    void processCheckout_shouldCreateOrderAndClearCart() throws Exception {
        mvc.perform(post("/cart/checkout")
                .param("customerName", "test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        List<Order> orders = orderRepository.findAll();

        assertThat(orders).hasSize(1);
        assertThat(orders.getFirst().getCustomerName()).isEqualTo("test");
        assertThat(orders.getFirst().getOrderItems().size()).isEqualTo(1);
        assertThat(orders.getFirst().getOrderItems().getFirst().getId()).isEqualTo(1L);
        assertThat(cartService.getCartItems()).isEmpty();
    }
}
