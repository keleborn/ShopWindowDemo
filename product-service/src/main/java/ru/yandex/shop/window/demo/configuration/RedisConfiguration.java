package ru.yandex.shop.window.demo.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.shop.window.demo.model.Product;

@Configuration
@EnableCaching
public class RedisConfiguration {
    @Bean
    public ReactiveRedisTemplate<String, Product> reactiveRedisTemplate(ReactiveRedisConnectionFactory redisConnectionFactory) {
        RedisSerializationContext<String, Product> context = RedisSerializationContext.<String, Product>newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(Product.class))
                .build();

        return new ReactiveRedisTemplate<>(redisConnectionFactory, context);
    }
}
