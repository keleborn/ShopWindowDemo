package ru.yandex.shop.window.demo.repository;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import ru.yandex.shop.window.demo.model.Product;

import java.math.BigDecimal;

public interface CustomProductRepository {
    Flux<Product> findAllByCriteria(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String namePrefix,
            String search,
            Pageable pageable
    );
}
