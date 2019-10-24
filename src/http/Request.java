package http;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Request {
    public String method;
    public String url;
    public String protocol;
    public String pragma;
    public String host;
    public String connection;
    public String upgrade_insecure_requests;
    public String user_agent;
    public String content_length;
    public String content_type;
    public String cache_control;
    public String sec_fetch_mode;
    public String sec_fetch_user;
    public String sec_fetch_site;
    public String origin;
    public String accept;
    public String accept_encoding;
    public String accept_language;
    public String boundary;
    public String referer;
    public Cookie cookie = null;
    public HttpSession session;
    public Map<String, String> paramater = new HashMap<>();

    public Request(List<String> httpHeader) {
        parse(httpHeader);
    }

    /**
     * @param httpHeader
     */
    private void parse(List<String> httpHeader) {
        this.method = httpHeader.get(0).split(" ")[0];
        this.url = httpHeader.get(0).split(" ")[1];
        this.protocol = httpHeader.get(0).split(" ")[2];

        for (int i = 1; i < httpHeader.size(); i++) {
            if (httpHeader.get(i).startsWith("Cookie")) {
                System.out.println(httpHeader.get(i));
                Cookie cookie = new Cookie(httpHeader.get(i));
                this.cookie = cookie;

                //Cookie的解析方法
                session = SessionContext.getSession(cookie.map.get("JSESSIONID"));

            } else {
                String[] data = httpHeader.get(i).split(":[ ]+");
                try {
                    Field field = Request.class.getField(data[0].replaceAll("-", "_").toLowerCase());
                    field.set(this, data[1]);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (this.content_type != null && this.content_type.contains("multipart/form-data")) {
            boundary = this.content_type.substring(content_type.indexOf("=") + 1);
            content_type = this.content_type.substring(0, content_type.indexOf(";"));
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", protocol='" + protocol + '\'' +
                ", pragma='" + pragma + '\'' +
                ", host='" + host + '\'' +
                ", connection='" + connection + '\'' +
                ", upgrade_insecure_requests='" + upgrade_insecure_requests + '\'' +
                ", user_agent='" + user_agent + '\'' +
                ", content_length='" + content_length + '\'' +
                ", content_type='" + content_type + '\'' +
                ", cache_control='" + cache_control + '\'' +
                ", sec_fetch_mode='" + sec_fetch_mode + '\'' +
                ", sec_fetch_user='" + sec_fetch_user + '\'' +
                ", sec_fetch_site='" + sec_fetch_site + '\'' +
                ", origin='" + origin + '\'' +
                ", accept='" + accept + '\'' +
                ", accept_encoding='" + accept_encoding + '\'' +
                ", accept_language='" + accept_language + '\'' +
                ", boundary='" + boundary + '\'' +
                ", referer='" + referer + '\'' +
                ", cookie=" + cookie.toString() +
                ", paramater=" + paramater +
                '}';
    }
}
