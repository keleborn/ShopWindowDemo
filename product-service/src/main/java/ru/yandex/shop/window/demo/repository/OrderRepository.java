package ru.yandex.shop.window.demo.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.shop.window.demo.model.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
