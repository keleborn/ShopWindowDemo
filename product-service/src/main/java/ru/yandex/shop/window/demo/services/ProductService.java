package ru.yandex.shop.window.demo.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;

import java.math.BigDecimal;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ReactiveRedisTemplate<String, Product> redisTemplate;

    public ProductService(ProductRepository productRepository, ReactiveRedisTemplate<String, Product> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    private static String redisKey(Long productId) {
        return "product::" + productId;
    }

    public Mono<Product> findById(Long productId) {
        return redisTemplate.opsForValue()
                .get(redisKey(productId))
                .switchIfEmpty(
                        productRepository.findById(productId)
                                .flatMap(product -> redisTemplate.opsForValue()
                                        .set(redisKey(productId), product)
                                        .thenReturn(product)));
    }

    public Flux<Product> findAllByCriteria(BigDecimal minPrice, BigDecimal maxPrice, String prefix, String search, Pageable pageable) {
        return productRepository.findAllByCriteria(minPrice, maxPrice, prefix, search, pageable);
    }

    public Mono<Product> save(Product product) {
        return productRepository.save(product)
                .flatMap(saved -> redisTemplate.opsForValue()
                        .set(redisKey(saved.getId()), saved)
                        .thenReturn(saved));
    }
}
