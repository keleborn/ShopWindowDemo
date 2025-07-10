package ru.yandex.shop.window.demo.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.repository.OrderRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderRepositoryIT {
    private Order order;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        order = orderRepository.save(new Order());
    }

    @Test
    void findById_shouldFindOrder() {
        Optional<Order> savedOrder = orderRepository.findById(order.getId());

        assertThat(savedOrder.isPresent()).isTrue();
        assertThat(savedOrder.get().getId()).isEqualTo(order.getId());
    }
}
