package ru.yandex.shop.window.demo.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.shop.window.demo.model.Product;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> priceGreaterOrEqual(BigDecimal min) {
        return (root, query, criteria) -> min != null ?
                criteria.greaterThanOrEqualTo(root.get("price"), min) : criteria.conjunction();
    }

    public static Specification<Product> priceLessOrEqual(BigDecimal max) {
        return (root, query, criteria) -> max != null ?
                criteria.lessThanOrEqualTo(root.get("price"), max) : criteria.conjunction();
    }

    public static Specification<Product> nameStartsWith(String prefix) {
        return (root, query, criteria) -> prefix != null ?
                criteria.like(criteria.lower(root.get("name")), prefix.toLowerCase() + "%") : criteria.conjunction();
    }

    public static Specification<Product> nameOrDescriptionConatins(String search) {
        return (root, query, criteria) -> {
            if (search == null || search.isBlank()) return criteria.conjunction();
            String pattern = "%" + search + "%";

            return criteria.or(criteria.like(criteria.lower(root.get("name")), pattern), criteria.like(criteria.lower(root.get("description")), pattern));
        };
    }

    public static Specification<Product> isAvailable() {
        return (root, query, criteria) -> criteria.equal(root.get("isAvailable"), true);
    }
}
