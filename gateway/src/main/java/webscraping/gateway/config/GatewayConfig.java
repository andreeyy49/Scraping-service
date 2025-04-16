package webscraping.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**").uri("lb://auth-service"))
                .route("users-service", r -> r.path("/api/v1/user/**").uri("lb://users-service"))
                .route("url-analyzer-service", r -> r.path("/api/v1/url-analyzer/**").uri("lb://url-analyzer-service"))
                .route("crawler-service", r -> r.path("/api/v1/crawler/**").uri("lb://crawler-service"))
                .route("playwright-service", r -> r.path("/api/v1/playwright/**").uri("lb://playwright-service"))
                .route("entity-vault-service", r -> r.path("/api/v1/entity-vault/**").uri("lb://entity-vault-service"))
                .build();
    }

    @Bean
    public GlobalFilter loggingFilter() {
        return (exchange, chain) -> {
            System.out.println("Request URI: " + exchange.getRequest().getURI());
            System.out.println("Request Method: " + exchange.getRequest().getMethod());

            exchange.getRequest().getHeaders().forEach((name, values) -> {
                System.out.println("Header: " + name + " Values: " + values);
            });

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("Response Status Code: " + exchange.getResponse().getStatusCode());
            }));
        };
    }

}