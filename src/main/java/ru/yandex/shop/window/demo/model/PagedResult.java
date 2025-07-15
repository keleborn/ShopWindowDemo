package ru.yandex.shop.window.demo.model;

import java.util.List;

public record PagedResult<T> (
        List<T> content,
        int currentPage,
        int pageSize,
        boolean hasNext
) {
    public boolean hasPrevious() {
        return currentPage > 0;
    }
}
