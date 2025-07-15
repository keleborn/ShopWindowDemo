package ru.yandex.shop.window.demo.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;
import ru.yandex.shop.window.demo.services.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
    private OrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderItemRepository);
    }

    @Test
    void getOrderWithItems_shouldReturnOrder() {
        Order order = new Order(1L, "test", LocalDateTime.now());
        OrderItem orderItem = new OrderItem(1L, order.getId(), "productName", BigDecimal.valueOf(10), 10, null);

        when(orderRepository.findById(order.getId())).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(Flux.just(orderItem));

        assertThat(orderService.getOrderWithItems(order.getId()).block().getOrder().getCustomerName()).isEqualTo("test");
        assertThat(orderService.getOrderWithItems(order.getId()).block().getOrderItems().getFirst().getProductName()).isEqualTo("productName");
    }

    @Test
    void findAllWithItems_shouldReturnOrders() {
        Order order1 = new Order(1L, "test1", LocalDateTime.now());
        OrderItem orderItem1 = new OrderItem(1L, order1.getId(), "productName1", BigDecimal.valueOf(10), 10, null);

        when(orderRepository.findAll()).thenReturn(Flux.just(order1));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem1));

        assertThat(orderService.findAllWithItems().block().size()).isEqualTo(1);
        assertThat(orderService.findAllWithItems().block().getFirst().getOrder().getCustomerName()).isEqualTo("test1");
    }
}
