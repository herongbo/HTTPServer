package server;

import annotation.AnnotationReader;
import config.Config;
import http.Request;
import http.Route;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.*;

public class BioWebServer implements WebServer {

    ExecutorService exec = initThreadPool();

    public BioWebServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(Config.PORT, 1);
        while (true) {
            Socket socket = serverSocket.accept();
            exec.submit(() -> new Client(socket));
        }
    }

    public ExecutorService initThreadPool() {
        ExecutorService exec = new ThreadPoolExecutor(Config.CORE_THREAD, Config.MAX_THREAD,
                0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(Config.MAX_CONNECTIONS),
                r -> new Thread(r, "BIOClient"), new ThreadPoolExecutor.DiscardPolicy());
        return exec;
    }
}

class Client {
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;
    Request request = null;

    public Client(Socket socket) throws IOException {
        System.out.println("a new connection");
        this.socket = socket;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
        read();
    }

    public void read() throws IOException {
        //初始化ByteReader，获取请求数据
        ByteReader byteReader = new ByteReader(is);

        // 获取Request对象
        Request request = byteReader.read();

        //待实现的路由方法

        AnnotationReader ar = null;
        try {
            ar = new AnnotationReader();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = request.url;
        //根据routeMap匹配
        if (ar.routeMap.containsKey(url)) {
            System.out.println("http 200 ok");
            System.out.println("命中" + ar.routeMap.get(url).processMethod);
            //调用方法
            Method method = ar.routeMap.get(url).processMethod;
            Class<?> returnType = method.getReturnType();
            Parameter[] paramTypes = method.getParameters();
            for (Parameter parameter : paramTypes) {
                System.out.println(parameter.getName());
                System.out.println(parameter.getType());
            }
        }
        //根据routeList，需要直接替换顺序查找？
        for (Route route : ar.routeList) {
            if (url.startsWith(route.url.substring(route.url.indexOf("*")))) {
                System.out.println("http 200 also");
            }
        }
        System.out.println("url: " + url);
        System.out.println();


        System.out.println("finished");
        os.write("HTTP/1.1 200 OK\n".getBytes());
        os.write("Content-Type: text/html; ".getBytes());
        os.write("charset=UTF-8\n\n".getBytes());
        os.write(("<html>\n<head>hello world</head>\n" +
                "<body>this is the test page of mycat web server" +
                new Date()
                + "</body></html>").getBytes());

        os.flush();
        this.socket.close();
    }
}
