package ru.yandex.shop.window.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
@AllArgsConstructor
public enum SortType {
    PRICE_ASC("price_asc", Sort.by(Sort.Direction.ASC, "price")),
    PRICE_DESC("price_desc", Sort.by(Sort.Direction.DESC, "price")),
    NAME_ASC("name_asc", Sort.by(Sort.Direction.ASC, "name")),
    NAME_DESC("name_desc", Sort.by(Sort.Direction.DESC, "name")),
    UNSORTED(null, Sort.unsorted());

    private final String key;
    private final Sort sort;

    public static SortType from(String key) {
        if (key == null || key.isBlank()) return UNSORTED;
        for (SortType sortType : SortType.values()) {
            if (key.equals(sortType.key)) {
                return sortType;
            }
        }
        return UNSORTED;
    }
}
