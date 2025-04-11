package webscraping.urlanalyzerservice.util;

import java.util.UUID;

public class UserContext {

    private final static ThreadLocal<UUID> userId = new ThreadLocal<>();
    private final static ThreadLocal<String> token = new ThreadLocal<>();

    public static void setUserId(UUID id) {
        userId.set(id);
    }

    public static UUID getUserId() {
        return userId.get();
    }

    public static void clear() {
        userId.remove();
        token.remove();
    }

    public static String getToken() {
        return token.get();
    }

    public static void setToken(String authToken) {
        token.set(authToken);
    }
}
