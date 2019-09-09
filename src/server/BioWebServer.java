package server;

import config.Config;
import http.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
    Request request;
    Socket socket;
    InputStream is = null;
    OutputStream os = null;

    public Client(Socket socket) throws IOException {
        System.out.println("a new connection");
        this.socket = socket;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
        read();
    }

    public void read() throws IOException {
        //如果没有表单怎么办？ 如果请求头在循环结束内没有接受完怎么办？
        ByteReader byteReader = new ByteReader(is);
        byteReader.read();
        System.out.println("finished");
        os.write("HTTP/1.1 200 OK\n".getBytes());
        os.write("<html>Hello world</html>".getBytes());
        this.socket.close();
    }
}
