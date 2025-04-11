package webscraping.userservice.util;

import java.util.UUID;

public class UserContext {

    private final static ThreadLocal<UUID> userId = new ThreadLocal<>();

    public static void setUserId(UUID id) {
        userId.set(id);
    }

    public static UUID getUserId() {
        return userId.get();
    }

    public static void clear() {
        userId.remove();
    }
}
