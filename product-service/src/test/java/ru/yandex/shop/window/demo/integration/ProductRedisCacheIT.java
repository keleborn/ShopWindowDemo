package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import ru.yandex.shop.window.demo.configuration.EmbeddedRedisConfiguration;
import ru.yandex.shop.window.demo.configuration.WebClientConfiguration;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.ProductService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({EmbeddedRedisConfiguration.class, WebClientConfiguration.class})
public class ProductRedisCacheIT {
    @Autowired
    private ReactiveRedisTemplate<String, Product> redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    void testRedisCache() {
        Product savedProduct = productRepository.save(new Product("test", "test", BigDecimal.valueOf(10), true)).block();

        productService.findById(savedProduct.getId()).block();

        assertThat(redisTemplate.opsForValue().get("product::" + savedProduct.getId())).isNotNull();
    }
}
