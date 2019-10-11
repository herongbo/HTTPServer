package run;

import annotation.AnnotationReader;
import config.ConfigReader;
import server.BioWebServer;
import server.WebServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class Start {
    public static void main(String[] args) throws IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        //读取配置文件
        ConfigReader configReader = new ConfigReader();

        AnnotationReader annoationReader = new AnnotationReader();

        WebServer server = new BioWebServer();
    }
}
