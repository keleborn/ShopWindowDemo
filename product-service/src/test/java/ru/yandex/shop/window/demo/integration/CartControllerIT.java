package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.client.api.PaymentApi;
import ru.yandex.shop.window.demo.client.model.PaymentResponse;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CartControllerIT extends AbstractAuthenticatedIT{
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

    @MockitoBean
    private PaymentApi paymentApi;

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
        authenticatedClient.post().uri("/cart/update/" + savedProduct.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("quantity", "10"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        assertThat(cartService.getCartItems().iterator().next().getQuantity()).isEqualTo(10);
    }

    @Test
    void processCheckout_shouldClearCartAndSaveOrderAndRedirectToOrderPage() {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);

        when(paymentApi.processPayment(any())).thenReturn(Mono.just(response));

        authenticatedClient.post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "John"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1");

        assertThat(cartService.getCartItems().size()).isEqualTo(0);
        assertThat(orderRepository.findAll().blockLast().getCustomerName()).isEqualTo("John");
    }

    @Test
    void processCheckout_shouldReturnErrorPageWhenServerError() {
        when(paymentApi.processPayment(any())).thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

        authenticatedClient.post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "John"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/payment-error");

        assertThat(cartService.getCartItems().size()).isNotEqualTo(0);
        assertThat(orderRepository.findAll().blockLast()).isEqualTo(null);
    }

    @Test
    void showCart_shouldReturnCartPage() {
        authenticatedClient.get().uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("test");
                    assertThat(html).contains("10");
                    assertThat(html).contains("<title>Корзина</title>");
                });
    }

    @Test
    void updateCart_shouldUpdateProductQuantity() {
        authenticatedClient.post().uri("/cart/update/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("quantity=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        assertThat(cartService.getCartItems().iterator().next().getQuantity()).isEqualTo(5);
    }

    @Test
    void checkout_shouldShowOrder() {
        authenticatedClient.get().uri("/cart/checkout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("order");
                });
    }
}
