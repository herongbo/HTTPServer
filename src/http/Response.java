package http;

//直接使用注解，不打算做web.xml了
//请求映射到 Controller，在配置中配置
//判断是静态文件还是

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

    public Response(OutputStream os) {
        writer = new OutputStreamWriter(os);
    }

    public OutputStreamWriter getWriter() {
        return writer;
    }

    public void httpSuccess() {
        try {
            writer.write("HTTP/1.1 200 OK\n");
            writer.write("Content-Type: text/html; ");
            writer.write("charset=UTF-8\n\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
