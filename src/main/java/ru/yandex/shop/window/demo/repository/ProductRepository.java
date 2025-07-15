package ru.yandex.shop.window.demo.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.shop.window.demo.model.Product;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long>, CustomProductRepository {
}
