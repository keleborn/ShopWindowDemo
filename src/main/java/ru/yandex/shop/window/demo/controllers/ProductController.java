package ru.yandex.shop.window.demo.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.shop.window.demo.enums.SortType;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final CartService cartService;

    public ProductController(ProductRepository productRepository, CartService cartService) {
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @GetMapping
    public String getProducts(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String sort,
                              @RequestParam(required = false) String search
                              ) {
        SortType sortType = SortType.from(sort);
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());
        Page<Product> productPage;
        if (search != null && !search.isBlank()) {
            productPage = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            productPage = productRepository.findByIsAvailableTrue(pageable);
        }
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        return "products";
    }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable long id, Model model) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("product", product);
        return "product";
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, @RequestParam int quantity) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cartService.addCartItem(product, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        cartService.removeCartItem(product);
        return "redirect:/cart";
    }
}
