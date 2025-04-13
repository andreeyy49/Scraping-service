package webscraping.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}