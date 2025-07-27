package ru.yandex.shop.window.demo.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAttribute {

    @ModelAttribute("authenticated")
    public boolean authenticated(Principal principal) {
        return principal != null;
    }

    @ModelAttribute("isKeycloakAuthorized")
    public boolean isKeycloakAuthorized(ServerWebExchange exchange) {
        Boolean flag = (Boolean) exchange.getAttribute("isKeycloakAuthorized");
        return flag != null && flag;
    }
}
