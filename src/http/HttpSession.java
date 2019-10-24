package http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    String id;
    Map<String,Object> map = new HashMap<>();

    public HttpSession(String sessionId) {
        this.id = sessionId;
    }

    public void put(String key,Object value){
        map.put(key,value);
    }

    public Object get(String key){
        return map.get(key);
    }

    public String getId() {
        return id;
    }
}
