package ru.yandex.shop.window.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.shop.window.demo.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
