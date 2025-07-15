package ru.yandex.shop.window.demo.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.controllers.CartController;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CartController.class)
public class CartControllerUnitTest {
    private List<CartItem> mockItems;
    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        mockItems = List.of(new CartItem(new Product(1L, "test", "description", BigDecimal.valueOf(10), true, null), 2));

        when(cartService.getCartItems()).thenReturn(mockItems);
        when(cartService.getTotal()).thenReturn(BigDecimal.valueOf(20));
    }

    @Test
    void showCart_shouldReturnCartPage() {
        webClient.get().uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("test");
                    assertThat(html).contains("20");
                    assertThat(html).contains("<title>Корзина</title>");
                });
    }

    @Test
    void updateCart_shouldUpdateProductQuantity() {
        webClient.post().uri("/cart/update/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("quantity=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        Mockito.verify(cartService).setQuantity(1L, 5);
    }

    @Test
    void checkout_shouldShowOrder() {
        webClient.get().uri("/cart/checkout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("order");
                });
    }

    @Test
    void checkout_shouldCreateOrder() {
        Order order = new Order();
        order.setCustomerName("test");
        order.setId(10L);

        when(cartService.getCartItems()).thenReturn(mockItems);
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.empty());
        doNothing().when(cartService).clearCart();

        webClient.post().uri("/cart/checkout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("customerName", "test"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/10");

        Mockito.verify(orderRepository).save(argThat(saved -> saved.getCustomerName().equals(order.getCustomerName())));

    }
}
