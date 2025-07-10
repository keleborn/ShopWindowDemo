package ru.yandex.shop.window.demo.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.shop.window.demo.model.CartItem;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.math.BigDecimal;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductRepository repo;

    @Autowired
    private CartService cartService;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        cartService.clearCart();
        repo.save(new Product("Test", "Description", BigDecimal.valueOf(100), true));
    }

    @Test
    void getProducts_shouldShowProductList() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Test")))
                .andExpect(model().attributeExists("productPage"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("pageSize"));
    }

    @Test
    void getProduct_shouldShowProductDetails() throws Exception {
        mvc.perform(get("/products/" + repo.findAll().getFirst().getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Description")))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void addToCart_shouldAddProductToCart() throws Exception {
        mvc.perform(post("/products/cart/add/{id}", repo.findAll().getFirst().getId())
                .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        Collection<CartItem> items = cartService.getCartItems();
        assertThat(items).hasSize(1);
        assertThat(items.iterator().next().getQuantity()).isEqualTo(2);
    }

    @Test
    void removeFromCart_shouldRemoveProductFromCart() throws Exception {
        mvc.perform(post("/products/cart/add/{id}", repo.findAll().getFirst().getId())
                .param("quantity", "2"));

        mvc.perform(post("/products/cart/remove/{id}", repo.findAll().getFirst().getId()));

        Collection<CartItem> items = cartService.getCartItems();
        assertThat(items).hasSize(0);
    }

    @Test
    void showCreateForm_shouldShowCreateForm() throws Exception {
        mvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("product"))
                .andExpect(view().name("product_form"));
    }

    @Test
    void processCreateForm_shouldSaveProduct() throws Exception {
        mvc.perform(multipart("/products")
                        .param("name", "Test")
                        .param("description", "Description")
                        .param("price", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }
}
