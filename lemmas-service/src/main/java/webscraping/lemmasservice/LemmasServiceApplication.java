package webscraping.lemmasservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LemmasServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemmasServiceApplication.class, args);
    }

}
