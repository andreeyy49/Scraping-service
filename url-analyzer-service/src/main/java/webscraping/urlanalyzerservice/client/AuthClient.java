package webscraping.urlanalyzerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import webscraping.urlanalyzerservice.config.FeignConfig;

@FeignClient(name = "authClient", url = "${external-api.authServiceUrl}", configuration = FeignConfig.class)
public interface AuthClient {

    @GetMapping(value = "/validate-token")
    boolean validateToken(@RequestHeader(value = "authorization") String authorizationHeader);
}
