package config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

public class ConfigReader {

    public ConfigReader() throws IllegalAccessException {
        Properties properties = new Properties();
        InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config/config.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("Config.properties not found,using the default config instead");
        }


        Config config = new Config();
        for (Field field : Config.class.getFields()) {
            String name = new StringBuffer().append("server").append(".")
                    .append(field.getName().replace("_", "-").toLowerCase())
                    .toString();

            String value = properties.getProperty(name);
            if (value != null && field.getType() == String.class) {
                field.set(config, value);
            } else if (value != null && field.getType() == int.class) {
                field.set(config, Integer.parseInt(value));
            }
        }

        checkConfig();
        printConfig();
    }

    public void highlight(Properties properties) {
        properties.get("server.mode");
        properties.get("server.port");
        properties.get("server.session-timeout");
        properties.get("server.max-connections");
        properties.get("server.max-thread");
        properties.get("server.core-thread");
        properties.get("server.servlet-config");
        properties.get("server.static");
        properties.get("server.view-perfix");
        properties.get("server.view-suffix");
        properties.get("server.charset");
    }

    /**
     * Print Server Config Details
     */
    public void printConfig() {
        System.out.println("server.port=" + Config.PORT);
        System.out.println("server.mode=" + Config.MODE);
        System.out.println("server.session-timeout=" + Config.SESSION_TIMEOUT);
        System.out.println("server.max-connections=" + Config.MAX_CONNECTIONS);
        System.out.println("server.max-thread=" + Config.MAX_THREAD);
        System.out.println("server.core-thread=" + Config.CORE_THREAD);
        System.out.println("server.servlet-config=" + Config.SERVLET_CONFIG);
        System.out.println("server.static=" + Config.STATIC);
        System.out.println("server.view-perfix=" + Config.VIEW_PERFIX);
        System.out.println("server.view-suffix=" + Config.VIEW_SUFFIX);
        System.out.println("server.charset="+Config.CHARSET);
    }

    public void checkConfig() {
        if (Config.PORT < 0 || Config.PORT > 65535) {
            System.err.println("post must be between 0 - 65535");
            System.exit(-1);
        }
        if (!Config.MODE.equals("bio") && !Config.MODE.equals("nio") && !Config.MODE.equals("aio")) {
            System.err.println("server mode not support");
            System.exit(-1);
        }
        if (Config.SESSION_TIMEOUT < 0) {
            System.err.println("invalid session-timeout");
            System.exit(-1);
        }
        if (Config.MAX_CONNECTIONS < 0) {
            System.err.println("invalid max-connections");
        }
    }
}
