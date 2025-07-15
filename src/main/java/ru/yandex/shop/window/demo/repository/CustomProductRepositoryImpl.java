package ru.yandex.shop.window.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.shop.window.demo.model.Product;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public CustomProductRepositoryImpl(DatabaseClient databaseClient, R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.databaseClient = databaseClient;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    @Override
    public Flux<Product> findAllByCriteria(BigDecimal minPrice, BigDecimal maxPrice, String namePrefix, String search, Pageable pageable) {
        StringBuilder sql = new StringBuilder("select * from products where is_available = true");
        Map<String, Object> params = new HashMap<>();

        if (namePrefix != null && !namePrefix.isBlank()) {
            sql.append(" and lower(name) like '").append(namePrefix.toLowerCase()).append("%'");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" and (lower(description) like '%").append(search.toLowerCase()).append("%'");
            sql.append(" or lower(name) like '%").append(search.toLowerCase()).append("%')");
        }
        if (minPrice != null) {
            sql.append(" and lower(price) >= ").append(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" and lower(price) <= ").append(maxPrice);
        }
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            sql.append(" order by ").append(order.getProperty()).append(" ").append(order.getDirection().name());
        }
        sql.append(" limit ")
                .append(pageable.getPageSize())
                .append(" offset ")
                .append(pageable.getPageNumber() * pageable.getPageSize());

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        return spec.map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(Product.class, row)).all();
    }
}
