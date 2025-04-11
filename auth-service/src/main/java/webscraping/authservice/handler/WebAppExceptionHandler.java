package webscraping.authservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import webscraping.authservice.exception.*;

import javax.naming.AuthenticationException;
import java.lang.IllegalArgumentException;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class WebAppExceptionHandler {

    @ExceptionHandler(value = RefreshTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseBody refreshTokenExceptionHandler(RefreshTokenException ex, WebRequest webRequest) {
        return buildResponse(ex, webRequest);
    }

    @ExceptionHandler(value = AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseBody alreadyExistsHandler(AlreadyExistsException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseBody notFoundHandler(EntityNotFoundException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseBody unauthorizedHandler(UnauthorizedException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponseBody illegalArgumentHandler(IllegalArgumentException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(value = DifferentPasswordsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseBody differentPasswordHandler(DifferentPasswordsException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseBody invalidTokenHandler(InvalidTokenException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(value = AuthenticationCredentialsNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseBody handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return buildResponse("Необходима полная аутентификация для доступа к этому ресурсу", request);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseBody handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildResponse("У вас нет прав для доступа к этому ресурсу", request);
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseBody globalExceptionHandler(Exception ex, WebRequest request) {
        log.error(ex.getMessage());
        return buildResponse("Что-то пошло не так", request);
    }

    private ErrorResponseBody buildResponse(Exception ex, WebRequest webRequest) {
        return ErrorResponseBody.builder()
                .message(ex.getMessage())
                .description(webRequest.getDescription(false))
                .build();
    }

    private ErrorResponseBody buildResponse(String message, WebRequest webRequest) {
        return ErrorResponseBody.builder()
                .message(message)
                .description(webRequest.getDescription(false))
                .build();
    }
}
