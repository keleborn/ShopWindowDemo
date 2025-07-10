package ru.yandex.shop.window.demo.test.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.shop.window.demo.model.Order;
import ru.yandex.shop.window.demo.repository.OrderRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerIT {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void getOrders_shouldShowOrders() throws Exception {
        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void getOrder_shouldShowOrder() throws Exception {
        Order order = orderRepository.save(new Order());
        mvc.perform(get("/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
        ;
    }
}
