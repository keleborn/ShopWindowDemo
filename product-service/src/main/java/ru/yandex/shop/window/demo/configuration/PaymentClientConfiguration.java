package ru.yandex.shop.window.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.shop.window.demo.client.ApiClient;
import ru.yandex.shop.window.demo.client.api.PaymentApi;

@Configuration
public class PaymentClientConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }

    @Bean
    public ApiClient paymentApiClient(WebClient webClient) {
        return new ApiClient(webClient)
                .setBasePath("http://localhost:8081");
    }

    @Bean
    public PaymentApi paymentApi(ApiClient apiClient) {
        return new PaymentApi(apiClient);
    }
}
