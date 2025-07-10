package ru.yandex.shop.window.demo.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.shop.window.demo.enums.SortType;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;
import ru.yandex.shop.window.demo.specification.ProductSpecification;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) BigDecimal minPrice,
                              @RequestParam(required = false) BigDecimal maxPrice,
                              @RequestParam(required = false) String alphabetFilter
                              ) {
        SortType sortType = SortType.from(sort);
        Pageable pageable = PageRequest.of(page, size, sortType.getSort());

        Specification<Product> specification = Specification.allOf(ProductSpecification.priceGreaterOrEqual(minPrice))
                .and(ProductSpecification.priceLessOrEqual(maxPrice))
                .and(ProductSpecification.nameStartsWith(alphabetFilter))
                .and(ProductSpecification.nameOrDescriptionConatins(search))
                .and(ProductSpecification.isAvailable());

        Page<Product> productPage = productRepository.findAll(specification, pageable);

        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("alphabetFilter", alphabetFilter);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minPrice", minPrice);
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

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return "product_form";
    }

    @PostMapping
    public String processCreateForm(@ModelAttribute Product product, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = UUID.randomUUID() + imageFile.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir");
            Path imagePath = Paths.get(uploadDir + "/uploads/images", fileName);

            Files.createDirectories(imagePath.getParent());
            imageFile.transferTo(imagePath.toFile());
            product.setImageUrl("/images/" + fileName);
        }

        productRepository.save(product);
        return "redirect:/products";
    }
}
