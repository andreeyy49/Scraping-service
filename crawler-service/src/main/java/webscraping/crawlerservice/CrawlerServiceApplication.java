package webscraping.crawlerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CrawlerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrawlerServiceApplication.class, args);
	}

}
