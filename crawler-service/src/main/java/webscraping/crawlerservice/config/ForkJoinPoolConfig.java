package webscraping.crawlerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

@Configuration
public class ForkJoinPoolConfig {

    @Bean(destroyMethod = "shutdown")
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }
}
