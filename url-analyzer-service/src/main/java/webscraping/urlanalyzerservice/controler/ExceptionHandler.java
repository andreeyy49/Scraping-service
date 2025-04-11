package webscraping.urlanalyzerservice.controler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import webscraping.urlanalyzerservice.dto.ErrorResponse;
import webscraping.urlanalyzerservice.exception.ConnectionException;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(value = ConnectionException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ErrorResponse refreshTokenExceptionHandler(ConnectionException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
