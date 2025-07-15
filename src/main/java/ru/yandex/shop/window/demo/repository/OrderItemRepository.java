package ru.yandex.shop.window.demo.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.shop.window.demo.model.OrderItem;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
    Flux<OrderItem> findByOrderId(Long orderId);
}
