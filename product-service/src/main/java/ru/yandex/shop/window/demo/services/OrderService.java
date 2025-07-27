package ru.yandex.shop.window.demo.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.model.OrderDto;
import ru.yandex.shop.window.demo.model.OrderItem;
import ru.yandex.shop.window.demo.repository.OrderItemRepository;
import ru.yandex.shop.window.demo.repository.OrderRepository;

import java.util.List;
import java.util.Objects;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Mono<OrderDto> getOrderWithItems(Long orderId) {
        Mono<Order> orderMono = orderRepository.findById(orderId);
        Flux<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return Mono.zip(orderMono, orderItems.collectList(), OrderDto::new);
    }

    public Mono<List<OrderDto>> findAllWithItems(String customerName) {
        return orderRepository.findAll()
                .sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .filter(order -> order.getCustomerName().equals(customerName))
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(items -> new OrderDto(order, items)))
                .collectList();
    }

    public Mono<Order> saveOrderWithItems(OrderDto orderDto) {
        return orderRepository.save(orderDto.getOrder())
                .flatMap(savedOrder -> {
                    Objects.requireNonNull(savedOrder.getId(), "Order id is null");
                    orderDto.getOrderItems().forEach(orderItem -> orderItem.setOrderId(savedOrder.getId()));
                    return Flux.fromIterable(orderDto.getOrderItems())
                            .flatMap(orderItemRepository::save)
                            .then(Mono.just(savedOrder));
                });
    }
}
