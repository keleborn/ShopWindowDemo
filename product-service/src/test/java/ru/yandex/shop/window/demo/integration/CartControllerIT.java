package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.client.ApiClient;
import ru.yandex.shop.window.demo.client.api.PaymentApi;
import ru.yandex.shop.window.demo.client.model.PaymentResponse;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CartControllerIT extends AbstractAuthenticatedIT {
    private Product savedProduct;
    private final String username = "test";

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
        cartService.clearCart(username);
        savedProduct = productRepository.saveAll(List.of(new Product("test1", "description1", BigDecimal.valueOf(10), true),
                new Product("test2", "description2", BigDecimal.valueOf(10), true))).blockLast();
        cartService.addCartItem(username, savedProduct, 5);
    }

    @Test
    void updateCart_shouldUpdateCartAndRedirectToCartPage() {
        authenticatedClient.post().uri("/cart/update/" + savedProduct.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("quantity", "10"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        assertThat(cartService.getCartItems(username).iterator().next().getQuantity()).isEqualTo(10);
    }

    @Test
    void processCheckout_shouldClearCartAndSaveOrderAndRedirectToOrderPage() {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);

        ApiClient mockApiClient = mock(ApiClient.class);
        when(paymentApi.getApiClient()).thenReturn(mockApiClient);
        when(paymentApi.processPayment(any())).thenReturn(Mono.just(response));

        authenticatedClient.mutate()
                .defaultCookie("SESSION", mockAccessToken())
                .build()
                .post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "John"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1");

        assertThat(cartService.getCartItems(username).size()).isEqualTo(0);
        assertThat(orderRepository.findAll().blockLast().getCustomerName()).isEqualTo("John");
    }

    @Test
    void processCheckout_shouldRedirectToOuath2LoginPageIfUserIsNotOauth2Authorized() {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(true);

        when(paymentApi.processPayment(any())).thenReturn(Mono.just(response));

        authenticatedClient.post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "John"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/oauth2/keycloak");
    }

    @Test
    void processCheckout_shouldReturnErrorPageWhenServerError() {
        ApiClient mockApiClient = mock(ApiClient.class);
        when(paymentApi.getApiClient()).thenReturn(mockApiClient);
        when(paymentApi.processPayment(any())).thenReturn(Mono.error(WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                "not found",
                HttpHeaders.EMPTY,
                null,
                null)));

        authenticatedClient.mutate()
                .defaultCookie("SESSION", mockAccessToken())
                .build()
                .post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "test"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/balance/404");

        assertThat(cartService.getCartItems(username).size()).isNotEqualTo(0);
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
    void showCart_shouldRedirectToLoginPageIfUserIsNotLoggedIn() {
        webClient.get().uri("/cart")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/login");
    }

    @Test
    void updateCart_shouldUpdateProductQuantityIsolated() {
        authenticatedClient.post().uri("/cart/update/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("quantity=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        cartService.addCartItem("John", new Product(1L, "First", "1234", BigDecimal.valueOf(1000), true, null), 10);
        assertThat(cartService.getCartItems(username).iterator().next().getQuantity()).isEqualTo(5);
    }

    @Test
    void checkout_shouldShowOrder() {
        authenticatedClient.mutate()
                .defaultCookie("SESSION", mockAccessToken())
                .build()
                .get().uri("/cart/checkout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("order");
                });
    }
}
