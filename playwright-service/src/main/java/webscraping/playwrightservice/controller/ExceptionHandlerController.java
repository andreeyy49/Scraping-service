package webscraping.playwrightservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import webscraping.playwrightservice.dto.ErrorResponse;
import webscraping.playwrightservice.exception.BadRequestException;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequest(BadRequestException e) {
        return new ErrorResponse(e.getMessage());
    }

}
