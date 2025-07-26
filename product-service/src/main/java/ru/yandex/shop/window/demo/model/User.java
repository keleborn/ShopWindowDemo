package ru.yandex.shop.window.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Getter
@Setter
public class User {
    @Id
    private Long id;

    private String username;
    private String password;
    private String roles;
}
