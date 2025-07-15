package ru.yandex.shop.window.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    private Long id;

    private String customerName;
    private LocalDateTime createdAt;

    public Order(String customerName, LocalDateTime createdAt) {
        this.customerName = customerName;
        this.createdAt = createdAt;
    }
}
