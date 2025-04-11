package webscraping.crawlerservice.util;

import java.util.UUID;

public class UserContext {

    private final static ThreadLocal<UUID> userId = new ThreadLocal<>();
    private final static ThreadLocal<String> tokenAuth = new ThreadLocal<>();

    public static void setUserId(UUID id) {
        userId.set(id);
    }

    public static UUID getUserId() {
        return userId.get();
    }

    public static void clear() {
        tokenAuth.remove();
        userId.remove();
    }

    public static String getToken() {
        return tokenAuth.get();
    }

    public static void setToken(String token) {
        tokenAuth.set(token);
    }
}
