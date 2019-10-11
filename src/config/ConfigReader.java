package config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * 读取config.properties
 *
 * @author JDUSER
 * @since 2019-9-25
 */
public class ConfigReader {

    private Properties properties = new Properties();
    private int MIN_PORT = 0;
    private int MAX_PORT = 65535;
    private String BIO_MODE = "bio";
    private String NIO_MODE = "nio";
    private String AIO_MODE = "aio";

    public ConfigReader() throws IllegalAccessException {

        doReadConfig();

        doCheckConfig();

        doPrintConfig();
    }

    /**
     * 读取配置信息
     *
     * @throws IllegalAccessException
     */
    private void doReadConfig() throws IllegalAccessException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");

        try {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("could not found Config.properties,using the default config instead");
        }


        Config config = new Config();

        for (Field field : Config.class.getFields()) {
            // 获取变量对应的配置项
            String fieldName = new StringBuffer().append("server").append(".")
                    .append(field.getName().replace("_", "-").toLowerCase())
                    .toString();

            String value = properties.getProperty(fieldName);

            if (value != null && field.getType() == String.class) {
                field.set(config, value);
            } else if (value != null && field.getType() == int.class) {
                field.set(config, Integer.parseInt(value));
            }
        }
    }

    /**
     * 配置信息代码高亮
     *
     * @param properties
     */
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
     * 打印配置信息
     */
    public void doPrintConfig() {
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
        System.out.println("server.charset=" + Config.CHARSET);
    }

    /**
     * 检查配置信息有效
     */
    public void doCheckConfig() {
        if (Config.PORT < MIN_PORT || Config.PORT > MAX_PORT) {
            System.err.println("port must be between 0 - 65535");
            System.exit(-1);
        }
        if (!Config.MODE.equals(BIO_MODE) && !Config.MODE.equals(NIO_MODE) && !Config.MODE.equals(AIO_MODE)) {
            System.err.println("server working mode not support");
            System.exit(-1);
        }
        if (Config.SESSION_TIMEOUT < 0) {
            System.err.println("invalid session-timeout");
            System.exit(-1);
        }
        if (Config.MAX_CONNECTIONS < 0) {
            System.err.println("invalid max-connections");
            System.err.println(-1);
        }
    }
}
