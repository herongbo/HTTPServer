package http;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JDUSER
 */
public class SessionContext {
    private static Map<String, HttpSession> map = new HashMap<>();

    public static HttpSession getSession(String sessionId) {
        return map.get(sessionId);
    }

    public static void setSession(String sessionId, HttpSession session) {
        map.put(sessionId, session);
    }

    public static void removeSession(String sessionId) {
        map.remove(sessionId);
    }
}
