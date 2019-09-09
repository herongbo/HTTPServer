package run;

import config.Config;
import config.ConfigReader;
import server.BioWebServer;
import server.WebServer;

import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IllegalAccessException, IOException {
        ConfigReader configReader = new ConfigReader();
        WebServer server = new BioWebServer();
    }
}
