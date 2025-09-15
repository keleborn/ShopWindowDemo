package ru.yandex.shop.window.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/products", "/products/new", "/products/*", "/login", "/login.html").permitAll()
                        .pathMatchers(HttpMethod.POST, "/products").permitAll()
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form.loginPage("/login")
                        .authenticationSuccessHandler(SecurityConfiguration::createRedirectToProductsResponse))
                .logout(logout -> logout.logoutSuccessHandler(SecurityConfiguration::onLogoutSuccess))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static Mono<Void> createRedirectToProductsResponse(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().set("Location", "/products");
        return response.setComplete();
    }

    private static Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        return exchange.getExchange().getSession()
                .doOnNext(session -> session.getAttributes().remove("access_token"))
                .then(createRedirectToProductsResponse(exchange, authentication));
    }
}