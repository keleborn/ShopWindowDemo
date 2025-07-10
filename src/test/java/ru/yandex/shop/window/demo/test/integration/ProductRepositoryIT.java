package ru.yandex.shop.window.demo.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryIT {

    private final Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

    @Autowired
    private ProductRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        repo.save(new Product("test", "description", BigDecimal.valueOf(100), true));
    }

    @Test
    void findAll_shouldFindAllProducts() {
        repo.save(new Product("test2", "description2", BigDecimal.valueOf(100), true));
        assertThat(repo.findAll()).hasSize(2);
    }

    @Test
    void findByName_shouldFindProductByName() {
        repo.save(new Product("test2", "description2", BigDecimal.valueOf(100), true));

        Page<Product> products = repo.findByName("test", pageable);
        assertThat(products).hasSize(1);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("test");
    }
}
