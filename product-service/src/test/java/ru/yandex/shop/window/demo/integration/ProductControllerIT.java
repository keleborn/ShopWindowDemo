package ru.yandex.shop.window.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductControllerIT extends AbstractAuthenticatedIT {
    private Product savedProduct;
    private final String username = "test";

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
        cartService.clearCart(username);
        savedProduct = productRepository.saveAll(List.of(new Product("test1", "description1", BigDecimal.valueOf(10), true),
                new Product("test2", "description2", BigDecimal.valueOf(10), true))).blockLast();
    }

    @Test
    void getProducts_shouldReturnPageWithProducts() {
        webClient.get().uri("/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("test1");
                    assertThat(html).contains("test2");
                    assertThat(html).contains("description1");
                    assertThat(html).contains("description2");
                    assertThat(html).contains("10");
                });
    }

    @Test
    void getProduct_shouldShowProductPage() {
        webClient.get().uri("/products/" + savedProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("test2");
                    assertThat(html).doesNotContain("description1");
                });
    }

    @Test
    void addToCart_shouldAddProductToCartAndRedirectToCartPage() {
        authenticatedClient.post().uri("/products/cart/add/" + savedProduct.getId())
                .bodyValue("quantity=5")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        assertThat(cartService.getCartItems(username).size()).isEqualTo(1);
        assertThat(cartService.getCartItems(username).iterator().next().getProduct().getId()).isEqualTo(savedProduct.getId());
    }

    @Test
    void removeFromCart_shouldRemoveProductFromCartAndRedirectToCartPage() {
        cartService.addCartItem(username, savedProduct, 15);

        authenticatedClient.post().uri("/products/cart/remove/" + savedProduct.getId())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        assertThat(cartService.getCartItems(username).size()).isEqualTo(0);
    }

    @Test
    void showCreateForm_shouldShowProductFormPage() {
        webTestClient.get().uri("/products/new")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(rs -> {
                    String html = rs.getResponseBody();
                    assertThat(html).contains("<title>Добавить продукт</title>");
                    assertThat(html).contains("product");
                });
    }

    @Test
    void processCreateForm_shouldSaveProductAndRedirectToProductsPage() {
        webTestClient.post().uri("/products")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("name", "test3")
                        .with("description", "description3")
                        .with("available", "true"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/products");

        assertThat(productRepository.findAll().blockLast().getName()).isEqualTo("test3");
    }
}
