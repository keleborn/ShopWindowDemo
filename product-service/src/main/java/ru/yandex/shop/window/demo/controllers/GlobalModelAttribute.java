package ru.yandex.shop.window.demo.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAttribute {

    @ModelAttribute("authenticated")
    public boolean authenticated(Principal principal) {
        return principal != null;
    }
}
