package ru.yandex.shop.window.demo.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.ProductService;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {

    @Mock
    private ReactiveRedisTemplate<String, Product> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, Product> operations;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findById_shouldReturnFromRedis() {
        Product expected = new Product("test", "test", BigDecimal.valueOf(10), true);
        expected.setId(1L);

        when(redisTemplate.opsForValue()).thenReturn(operations);
        when(operations.get("product::1")).thenReturn(Mono.just(expected));
        when(productRepository.findById(1L)).thenReturn(Mono.just(expected));

        productService.findById(1L).block();

        verify(operations).get("product::1");
        verify(productRepository).findById(1L);
        verify(operations, never()).set(any(), any());
    }

    @Test
    void findById_shouldQueryDbAndCache() {
        Product expected = new Product("test", "test", BigDecimal.valueOf(10), true);
        expected.setId(1L);

        when(redisTemplate.opsForValue()).thenReturn(operations);
        when(operations.get("product::1")).thenReturn(Mono.empty());
        when(operations.set("product::1", expected)).thenReturn(Mono.just(true));
        when(productRepository.findById(1L)).thenReturn(Mono.just(expected));

        productService.findById(1L).block();

        verify(operations).get("product::1");
        verify(productRepository).findById(1L);
        verify(operations).set("product::1", expected);
    }

    @Test
    void save_shouldInsertProductToDbAndCache() {
        Product toSave = new Product("test", "test", BigDecimal.valueOf(10), true);
        toSave.setId(1L);

        when(productRepository.save(toSave)).thenReturn(Mono.just(toSave));
        when(redisTemplate.opsForValue()).thenReturn(operations);
        when(operations.set("product::1", toSave)).thenReturn(Mono.just(true));

        productService.save(toSave).block();

        verify(operations).set("product::1", toSave);
        verify(productRepository).save(toSave);
    }
}
