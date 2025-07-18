package ru.yandex.shop.window.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderDto {
    private Order order;
    private List<OrderItem> orderItems;
}
