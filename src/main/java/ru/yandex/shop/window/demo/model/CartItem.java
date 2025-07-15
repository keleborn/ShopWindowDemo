package ru.yandex.shop.window.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public OrderItem toOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductName(product.getName());
        orderItem.setPrice(product.getPrice());
        orderItem.setImageUrl(product.getImageUrl());
        orderItem.setQuantity(quantity);
        return orderItem;
    }
}

