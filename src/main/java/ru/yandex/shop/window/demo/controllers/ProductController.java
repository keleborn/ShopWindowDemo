package ru.yandex.shop.window.demo.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.shop.window.demo.enums.SortType;
import ru.yandex.shop.window.demo.model.CartForm;
import ru.yandex.shop.window.demo.model.PagedResult;
import ru.yandex.shop.window.demo.model.Product;
import ru.yandex.shop.window.demo.repository.ProductRepository;
import ru.yandex.shop.window.demo.services.CartService;

import java.io.IOException;
import java.math.BigDecimal;
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
    public Mono<String> getProducts(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String sort,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) BigDecimal minPrice,
                              @RequestParam(required = false) BigDecimal maxPrice,
                              @RequestParam(required = false) String alphabetFilter
                              ) {
        SortType sortType = SortType.from(sort);
        Pageable pageable = PageRequest.of(page, size + 1, sortType.getSort());

        return productRepository.findAllByCriteria(minPrice, maxPrice, search, alphabetFilter, pageable)
                .collectList()
                .map(products -> {
                            boolean hasNext = products.size() > size;
                            if (hasNext) {
                                products = products.subList(0, size);
                            }
                            return new PagedResult<>(products, page, size, hasNext);
                        })
                .map(pagedResult -> {
                    model.addAttribute("productPage", pagedResult.content());
                    model.addAttribute("currentPage", pagedResult.currentPage());
                    model.addAttribute("pageSize", pagedResult.pageSize());
                    model.addAttribute("hasNext", pagedResult.hasNext());
                    model.addAttribute("hasPrevious", pagedResult.hasPrevious());
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("alphabetFilter", alphabetFilter);
                    model.addAttribute("maxPrice", maxPrice);
                    model.addAttribute("minPrice", minPrice);
                   return "products";
                });
    }

    @GetMapping("/{id}")
    public Mono<String> getProduct(@PathVariable long id, Model model) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product";
                });
    }

    @PostMapping(value = "/cart/add/{id}")
    public Mono<String> addToCart(@PathVariable Long id, CartForm form) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .doOnNext(product -> cartService.addCartItem(product, form.getQuantity()))
                .thenReturn("redirect:/cart");
    }

    @PostMapping("/cart/remove/{id}")
    public Mono<String> removeFromCart(@PathVariable Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .doOnNext(cartService::removeCartItem)
                .thenReturn("redirect:/cart");
    }

    @GetMapping("/new")
    public Mono<String> showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        return Mono.just("product_form");
    }

    @PostMapping
    public Mono<String> processCreateForm(@ModelAttribute Product product, @RequestParam(value = "imageFile", required = false) FilePart imageFile) throws IOException {
        if (imageFile != null && !imageFile.filename().isBlank()) {
            String fileName = UUID.randomUUID() + imageFile.filename();
            String uploadDir = System.getProperty("user.dir");
            Path imagePath = Paths.get(uploadDir + "/uploads/images", fileName);

            imageFile.transferTo(imagePath)
                    .doOnSuccess(unused -> product.setImageUrl("/images/" + fileName));
        }

        return productRepository.save(product)
                .thenReturn("redirect:/products");
    }
}
