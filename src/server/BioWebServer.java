package server;

import annotation.AnnotationReader;
import annotation.DispatchServlet;
import config.Config;
import http.Request;
import http.Response;
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

    public ExecutorService exec = initThreadPool();

    public DispatchServlet dispatchServlet;

    public BioWebServer(DispatchServlet dispatchServlet) throws IOException {
        this.dispatchServlet = dispatchServlet;

        ServerSocket serverSocket = new ServerSocket(Config.PORT, 1);
        while (true) {
            Socket socket = serverSocket.accept();
            exec.submit(() -> new Client(socket, this.dispatchServlet));
        }
    }

    public ExecutorService initThreadPool() {
        ExecutorService exec = new ThreadPoolExecutor(Config.CORE_THREAD, Config.MAX_THREAD,
                60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(Config.MAX_CONNECTIONS),
                r -> new Thread(r, "BIOClient"), new ThreadPoolExecutor.DiscardPolicy());
        return exec;
    }
}

class Client {
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;
    Request request = null;
    Response response = null;
    DispatchServlet dispatchServlet;

    public Client(Socket socket, DispatchServlet dispatchServlet) throws IOException {
        System.out.println("a new connection");
        this.socket = socket;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
        this.dispatchServlet = dispatchServlet;
        this.response = new Response(os);
        read();
    }

    public void read() throws IOException {
        //初始化ByteReader，获取请求数据
        ByteReader byteReader = new ByteReader(is);

        // 获取Request对象
        Request request = byteReader.read();

        // 请求分发
        System.out.println("请求分发");

        dispatchServlet.handlerAdapter(request, response);

        os.flush();
        this.socket.close();
    }
}
