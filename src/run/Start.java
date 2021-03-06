package run;

import annotation.DispatchServlet;
import config.ConfigReader;
import server.impl.BioWebServer;
import server.WebServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class Start {
    public static void main(String[] args) throws IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        //读取配置文件
        ConfigReader configReader = new ConfigReader();

        DispatchServlet dispatchServlet = new DispatchServlet();
        System.out.println("dispatcherservlet " + dispatchServlet);

        WebServer server = new BioWebServer(dispatchServlet);
    }
}
