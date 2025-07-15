package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductRepositoryIT {
    private Pageable pageable;
    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
        productRepository.saveAll(List.of(
                new Product("1_test", "description1", BigDecimal.valueOf(10), true),
                new Product("2_test", "description2", BigDecimal.valueOf(100), true),
                new Product("3_test", "description3", BigDecimal.valueOf(100), false)
        )).blockLast();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findAllByCriteria_shouldFindAllAvailableProducts() {
        Flux<Product> savedProducts = productRepository.findAllByCriteria(null, null, null, null, pageable);

        assertThat(savedProducts.collectList().block().size()).isEqualTo(2);
    }

    @Test
    void findAllByCriteria_shouldFindAllAvailableProductsWithSearchCriteria() {
        Flux<Product> savedProducts = productRepository.findAllByCriteria(null, null, null, "1_t", pageable);

        assertThat(savedProducts.collectList().block().size()).isEqualTo(1);
        assertThat(savedProducts.collectList().block().getFirst().getDescription()).isEqualTo("description1");
    }

    @Test
    void findAllByCriteria_shouldFindAllAvailableProductsWithPriceCriteria() {
        Flux<Product> savedProducts = productRepository.findAllByCriteria(BigDecimal.valueOf(11), BigDecimal.valueOf(99), null, null, pageable);

        assertThat(savedProducts.collectList().block()).isEmpty();

        savedProducts = productRepository.findAllByCriteria(BigDecimal.valueOf(2), BigDecimal.valueOf(99), null, null, pageable);

        assertThat(savedProducts.collectList().block().size()).isEqualTo(1);
        assertThat(savedProducts.collectList().block().get(0).getDescription()).isEqualTo("description1");
    }

    @Test
    void findAllByCriteria_shouldFindAllAvailableProductsWithNamePrefixCriteria() {
        Flux<Product> savedProducts = productRepository.findAllByCriteria(null, null, "2", null, pageable);

        assertThat(savedProducts.collectList().block().size()).isEqualTo(1);
        assertThat(savedProducts.collectList().block().get(0).getDescription()).isEqualTo("description2");
    }
}
