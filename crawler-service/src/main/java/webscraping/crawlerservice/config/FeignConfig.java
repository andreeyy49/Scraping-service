package webscraping.crawlerservice.config;

import feign.RequestInterceptor;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import webscraping.crawlerservice.util.UserContext;

import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
@Slf4j
public class FeignConfig {

//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return requestTemplate -> {
//            String token = UserContext.getToken();
//            log.info("token is: {}", token);
//            if (token != null) {
//                requestTemplate.header("Authorization", token);
//            }
//        };
//    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, SECONDS.toMillis(1), 5);
    }

}
