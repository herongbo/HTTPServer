package server;

import config.Config;
import http.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.ServerError;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        System.out.println("init");
        this.socket = socket;
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
        read();
    }

    public void read() throws IOException {
        // 一次读入
        byte[] buffer = new byte[1024 * 1024 * 10];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<String> headerList = new ArrayList<>();
        List<Object> bodyList = new ArrayList<>();


        boolean header = true;
        boolean body = false;

        //如果没有表单怎么办？ 如果请求头在循环结束内没有接受完怎么办？
        ByteReader byteReader = new ByteReader(is);
        byteReader.read();
        out.write("http 200".getBytes());

        //如果放弃上传文件功能的文件就容易很多
//        while (true) {
//            int len = is.read(buffer);
//            if (len == -1) {
//                socket.close();
//                Thread.currentThread().stop();
//            }
//            System.out.println(len);
//            int i = 0;
//            int j = 0;
//            // /r/r标志一次换行 请求的结尾没有换行符
//            headerloop:
//            for (j = 0; j < len; j++) {
//                switch (buffer[j]) {
//                    case '\n':
//                        System.out.println("\\n" + j);
//                        i = j + 1;
//                        break;
//                    case '\r':
//                        String data = merge(buffer, i, j);
//                        if (header) {
//                            headerList.add(data);
//                        }
//                        System.out.println(data);
//                        if (data.isEmpty()) {
//                            header = false;
//                            j++;
//                            request = new Request(headerList);
//                            System.out.println(request);
//                            break headerloop;
//                        }
//                        i = j + 1;
//                        System.out.println("\\r" + j);
//                        break;
//                    default:
//                }
//            }
//            System.out.println("处理处理");
//            int k = j;
//            int l = 0;
//            //处理body
//
//            System.out.println("len:" + len);
//            bodyLoop:
//            for (; j < len - 1; j++) {
//                //打印字段
//                if (buffer[j] == '\n') {
//                    System.err.println("\\n" + " " + buffer[j] + " " + j);
//                } else if (buffer[j] == '\r') {
//                    System.err.println("\\r" + " " + buffer[j] + " " + j);
//                } else {
//                    System.err.println((char) buffer[j] + " " + buffer[j] + " " + j);
//                }
//                if (buffer[j] == '\r' && buffer[j + 1] == '\n') {
//                    System.out.println("yes");
//                    l = j;
//                    String str = merge(buffer, k, l);
//                    if (str.contains(request.boundary)) {
//                        System.err.println(str);
//                        System.err.println("boundary");
//                        if (out.toByteArray().length > 0) {
//                            byte[] data = out.toByteArray();
//                            out.reset();
//                            File f = new File("C:\\Users\\JDUSER\\Desktop\\what\\" + new Date().hashCode() + ".jpg");
//                            System.out.println(f);
//                            f.createNewFile();
//                            FileOutputStream is = new FileOutputStream(f);
//                            is.write(data);
//                            is.flush();
//                            is.close();
//                            System.out.println("file saved" + f);
//                        }
//
//
//                    } else if (str.startsWith("Content-Disposition: form-data;")) {
//                        System.err.println(str);
//                    } else if (str.startsWith("Content-Type:")) {
//                        j++;
//                        j++;
//                        System.err.println(str);
//                    } else {
//                        System.err.println(merge(buffer, k, l));
//                        out.write(buffer, k, l);
//                        out.write("\r\n".getBytes());
//                    }
//                    k = j + 2;
//                    j++;
//                }
//            }
//            System.out.println("end");
//            System.out.println(merge(buffer, i, len));
//            System.out.println("##\n\n\n\n\n\n" + new String(buffer));
//        }
    }
}
