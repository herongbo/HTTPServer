package http;

//直接使用注解，不打算做web.xml了
//请求映射到 Controller，在配置中配置
//判断是静态文件还是

import config.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/*
考虑一下总体的逻辑，先收到请求
？sessionid
 根据请求作出相应
 static 文件夹的优先级最高
 其次是controller
 */
public class Response {
    // Response,提供了输出流
    OutputStreamWriter writer;
    OutputStream outputStream;

    public static String Error_500 = "HTTP/1.1 500 Server error\r\n" + "Content-Type: text/html\r\n" + "\r\n"
            + "<html><center><br> <h2>500 Error error on Server </h2> <br> "
            + "<hr style='height:1px;border:none;border-top:1px solid #0066CC;' /> <br> "
            + "<label>Mini web Server</label></center><html>";

    public static String Error_404 = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type: text/html\r\n" + "\r\n"
            + "<html><center><br> <h2>404 Error File Not Found </h2> <br> "
            + "<hr style='height:1px;border:none;border-top:1px solid #0066CC;' /> <br> "
            + "<label>Mini web Server</label><center><html>";

    public static String Http_success = "\"HTTP/1.1 200 OK\\n" + "charset=" + Config.CHARSET + "\n\n";

    public Response(OutputStream os) {
        this.writer = new OutputStreamWriter(os);
        this.outputStream = os;
    }

    public OutputStreamWriter getWriter() {
        return writer;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void httpSuccess() {
        try {
            writer.write(Http_success);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void http404() {
        try {
            writer.write(Error_404);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void http500() {
        try {
            writer.write(Error_500);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
