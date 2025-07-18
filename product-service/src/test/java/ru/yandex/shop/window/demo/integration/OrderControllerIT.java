package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class OrderControllerIT {
    private Order order;
    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        order = orderRepository.saveAll(List.of(
                new Order("test1", LocalDateTime.now()),
                new Order("test2", LocalDateTime.now())
        )).blockLast();
        orderItemRepository.saveAll(List.of(
                new OrderItem(order.getId(), "name1", BigDecimal.valueOf(10), 10, null),
                new OrderItem(order.getId(), "name2", BigDecimal.valueOf(10), 10, null)
        )).blockLast();
    }

    @Test
    void getOrders_shouldReturnAllOrders() {
        webClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("<title>Заказы</title>");
                    assertThat(html).contains("Заказ <span>" + order.getId());
                });
    }

    @Test
    void getOrder_shouldReturnOrderById() {
        webClient.get().uri("/orders/" + order.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("<strong>Дата заказа: </strong>");
                    assertThat(html).contains("name2");
                });
    }
}
