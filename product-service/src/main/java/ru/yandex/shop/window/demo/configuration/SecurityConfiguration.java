package ru.yandex.shop.window.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    private static final String KEYCLOAK_LOGOUT_URL = "http://localhost:8083/realms/shop/protocol/openid-connect/logout";
    private static final String POST_LOGOUT_REDIRECT_URL = "post_logout_redirect_uri=http://localhost:8080/products";

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
                .logout(logout -> logout.logoutSuccessHandler(SecurityConfiguration::createRedirectToProductsResponse))
                //.oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler((SecurityConfiguration::createRedirectToProductsResponse)))
                //.logout(logout -> logout.logoutSuccessHandler((this::handleLogout)))
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

    private Mono<Void> handleLogout(WebFilterExchange exchange, Authentication authentication) {
        return exchange.getExchange()
                .getSession()
                .doOnNext(WebSession::invalidate)
                .then(Mono.defer(() -> {
//                    OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
//                    OidcUser oidcUser = (OidcUser) authToken.getPrincipal();
//                    String idToken = oidcUser.getIdToken().getTokenValue();
//                    String logoutUrl = KEYCLOAK_LOGOUT_URL + "?id_token_hint=" + idToken + "&" + POST_LOGOUT_REDIRECT_URL;
                    ServerHttpResponse response = exchange.getExchange().getResponse();
                    ResponseCookie deleteCookie = ResponseCookie.from("SESSION", "")
                            .path("/")
                            .maxAge(Duration.ZERO)
                            .build();
                    response.setStatusCode(HttpStatus.FOUND);
                    response.addCookie(deleteCookie);
                    //response.getHeaders().setLocation(URI.create(logoutUrl));
                    return response.setComplete();
                }));
    }
}

