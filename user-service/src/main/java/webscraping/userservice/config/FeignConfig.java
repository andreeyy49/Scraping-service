package webscraping.userservice.config;

import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
public class FeignConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, SECONDS.toMillis(1), 5);
    }

    @Bean
    public RequestInterceptor userAgentInterceptor() {
        return requestTemplate -> requestTemplate.header("User-Agent", "SocialNetworkGeo/1.0 (andreeyy49@gmail.com)");
    }
}
