package http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cookie {

    Map<String, String> map = new HashMap<>(10);

    public Cookie(String data) {
        parseCookie(data);
    }

    /**
     * 从原始数据解析Cookie
     *
     * @param data
     */
    public void parseCookie(String data) {
        int i = data.indexOf(":");
        String cookie = data.substring(i + 2);
        String[] cookies = cookie.split("; ");
        for (String string : cookies) {
            int j = string.indexOf("=");
            String key = string.substring(0, j);
            String value = string.substring(j + 1);
            map.put(key, value);
        }
    }

    /**
     * @param key
     * @param value
     */
    public void setCookie(String key, String value) {
        map.put(key, value);
    }

    /**
     * @param key
     * @return Cookie
     */
    public String getCookie(String key) {
        return map.get(key);
    }

    @Override
    public String toString() {
        List<String> data = new ArrayList<>();
        map.entrySet().forEach(e -> data.add(e.getKey() + " : " + e.getValue()));
        return data.toString();
    }
}
