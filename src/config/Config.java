package config;

public class Config {
    public static int PORT = 8080;
    public static int SESSION_TIMEOUT = 60;
    public static int MAX_CONNECTIONS = 1500;
    public static int MAX_THREAD = 50;
    public static int CORE_THREAD = 12;
    public static String SERVLET_CONFIG = "web.xml";
    public static String STATIC = "/static";
    public static String MODE = "BIO";
    public static String VIEW_PERFIX = "/WEB-INF/jsp";
    public static String VIEW_SUFFIX = ".jsp";
    public static String CHARSET = "utf-8";
}
