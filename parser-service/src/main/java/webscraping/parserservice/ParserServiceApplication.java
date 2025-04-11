package webscraping.parserservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ParserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParserServiceApplication.class, args);
    }

}
