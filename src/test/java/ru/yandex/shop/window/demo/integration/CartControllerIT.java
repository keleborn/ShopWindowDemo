package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class CartControllerIT {
    private Product savedProduct;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        cartService.clearCart();
        savedProduct = productRepository.saveAll(List.of(new Product("test1", "description1", BigDecimal.valueOf(10), true),
                new Product("test2", "description2", BigDecimal.valueOf(10), true))).blockLast();
        cartService.addCartItem(savedProduct, 5);
    }

    @Test
    void updateCart_shouldUpdateCartAndRedirectToCartPage() {
        webClient.post().uri("/cart/update/" + savedProduct.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("quantity", "10"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        assertThat(cartService.getCartItems().iterator().next().getQuantity()).isEqualTo(10);
    }

    @Test
    void processCheckout_shouldClearCartAndSaveOrderAndRedirectToOrderPage() {
        webClient.post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "John"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1");

        assertThat(cartService.getCartItems().size()).isEqualTo(0);
        assertThat(orderRepository.findAll().blockLast().getCustomerName()).isEqualTo("John");
    }
}
