package http;

//直接使用注解，不打算做web.xml了
//请求映射到 Controller，在配置中配置
//判断是静态文件还是

import config.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.chrono.HijrahDate;
import java.util.Date;

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

    public static String Http_200_Html = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html; charset=" + Config.CHARSET + "\r\n";

    public static String Http_200 = "HTTP/1.1 200 OK\r\n" + "charset=" + Config.CHARSET + "\r\n";

    public static String Header_end = "\r\n";

    public static String Header_extra = "";

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

    public void setCookie(String key, String value) {
        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 3600);
        String str = "Set-Cookie: " + key + "=" + value + "; Expires=" + date.toString() + "; Path=/\n";
        Header_extra += str;
    }

    public void httpSuccessHtml() {
        try {
            System.out.println(Http_200_Html + Header_extra + Header_end);
            writer.write(Http_200_Html);
            writer.write(Header_extra);
            writer.write(Header_end);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void httpSuccess() {
        try {
            System.out.println(Http_200 + Header_extra + Header_end);
            writer.write(Http_200);
            writer.write(Header_extra);
            writer.write(Header_end);
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
