package webscraping.parserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "authClient", url = "${external-api.authServiceUrl}")
public interface AuthClient {

    @GetMapping(value = "/validate-token")
    boolean validateToken(@RequestHeader(value = "authorization") String authorizationHeader);
}
