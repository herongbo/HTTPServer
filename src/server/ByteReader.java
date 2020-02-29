package server;

import config.Config;
import http.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteReader {

    // 设置缓冲区大小
    static int BUFFER_SIZE = 65536;
    long start = System.currentTimeMillis();


    byte[] buffer;
    InputStream input;
    Request request = null;

    public ByteReader(InputStream input, int bufferSize) {
        this.input = input;
        buffer = new byte[bufferSize];
    }

    public ByteReader(InputStream input) {
        this(input, BUFFER_SIZE);
    }

    public Request read() throws IOException {
        start = System.currentTimeMillis();

        // 接收请求
        receiveData();

        // 解析请求
        parseData();

        return this.request;
    }

    public void receiveData() throws IOException {
        System.err.println("receive data...");
        // 数据总量
        long size = 0;
        // 数据合并到ByteArrayOutputStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // 接收数据
        while (true) {
            // 将一次读入的数据写入ByteArrayInputStream
            int len = input.read(buffer);
            bos.write(buffer, 0, len);
            size += len;

            // 判断是否是传输文件造成超时
            if (input.available() == 0 && size > BUFFER_SIZE) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 停止接收数据
                if (input.available() == 0) {
                    break;
                }
            } else if (input.available() == 0 && size < BUFFER_SIZE) {
                break;
            }
        }

        buffer = bos.toByteArray();
        System.out.println("数据接收用时： " + (System.currentTimeMillis() - start));
    }

    public void parseData() throws IOException {
        int pre = 0;
        int temp = 0;

        List<String> headerList = new ArrayList<>();
        ByteArrayOutputStream fos = new ByteArrayOutputStream();

        int i;
        for (i = 0; i < buffer.length; i++) {
            if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                String str = new String(buffer, pre, i - pre);
                // \r\n连续出现两次，header解析结束
                if (i - pre <= 1) {
                    break;
                }
                // 指针后移2个位置
                pre = i + 2;
                headerList.add(str);
            }
        }

        // 生成Request对象
        request = new Request(headerList);
        System.out.println(request);
        System.out.println("header解析结束 " + (System.currentTimeMillis() - start));

        //重置指针，开始解析body
        temp = pre;
        String name = null;
        String contentDesposition = null;
        String fileName = null;

        // 处理文件表单
        if (request.content_type != null && request.content_type.equals("multipart/form-data")) {
            for (; i < buffer.length; i++) {
                if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                    // 匹配数据边界 boundary，从第二个开始保存文件
                    if (byteCheckContains(buffer, temp, i, request.boundary.getBytes(), '-')) {
                        System.err.println("processing with file ");

                        //判断是否是第一个boundary
                        if (temp - 2 - pre > 0) {
                            // multipart/form-data中添加的是文件
                            if (fileName != null) {
                                //信息写入ByteArrayOutputStream
                                fos.write(buffer, pre, temp - 2 - pre);
                                byte[] data = fos.toByteArray();
                                fos.reset();

                                // 保存数据到临时文件
                                File file = saveTempFile(fileName, data);
                            } else {
                                //multipart/form-data 添加的是字符串
                                String data = new String(buffer, pre + 2, temp - 2 - pre, Config.CHARSET);
                                System.out.println(name + ":" + data);
                            }
                        }
                        temp = i + 2;
                        pre = i + 2;
                    } else if (byteCheckStartWith(buffer, temp, i, "Content-Disposition: form-data;".getBytes())) {

                        // 内容是Content-Disposition描述信息
                        String str = new String(buffer, temp, i - temp, Config.CHARSET);
                        System.out.println("Content-disposition: " + str);

                        String[] contents = str.split(";");
                        if (contents.length == 3) {
                            //文件类型
                            contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                            name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                            fileName = contents[2].substring(contents[2].indexOf("\"") + 1, contents[2].length() - 1);
                        } else if (contents.length == 2) {
                            //字符表单
                            contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                            name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                            fileName = null;
                        }
                        temp = i + 2;
                        pre = i + 2;
                    } else if (byteCheckStartWith(buffer, temp, i, "Content-Type:".getBytes())) {
                        //读取Content-Type字段后跳过一行空白行
                        pre = i + 4;
                        temp = i + 4;
                        i += 4;
                    } else {
                        //读取到文件中间，移动temp指针
                        temp = i + 2;
                    }
                }
            }
        } else if (request.content_type == null && request.method.equals("GET") && request.url.contains("?")) {
            // 处理get表单
            String str = request.url.substring(request.url.indexOf("?") + 1);
            request.url = request.url.substring(0, request.url.indexOf("?"));

            // 处理get表单
            String[] data = str.split("&");
            for (String s : data) {
                String key = s.substring(0, s.indexOf("="));
                String value = s.substring(s.indexOf("=") + 1);
                request.paramater.put(key, value);
            }
            Arrays.stream(data).forEach(System.out::println);
        } else if (request.content_type != null && request.content_type.startsWith("application/x-www-form-urlencoded")) {
            //处理post表单
            System.out.println("post表单");
            String str = new String(buffer, pre + 2, buffer.length - pre - 2);
            String[] data = str.split("&");

            // 处理表单内容
            for (String s : data) {
                String key = s.substring(0, s.indexOf("="));
                String value = s.substring(s.indexOf("=") + 1);
                request.paramater.put(key, value);
            }
            Arrays.stream(data).forEach(System.out::println);
        }
        System.err.println("表单解析用时：" + (System.currentTimeMillis() - start));
    }

    /**
     * 文件保存至临时位置
     *
     * @param fileName
     * @param data
     * @return
     * @throws IOException
     */
    public File saveTempFile(String fileName, byte[] data) throws IOException {

        String basePath = this.getClass().getClassLoader().getResource("").getPath().substring(1) + "upload_temp/";
        System.out.println(basePath);

        if (!new File(basePath).exists()) {
            new File(basePath).mkdirs();
        }
        File file = new File(basePath + fileName);
        file.createNewFile();

        FileOutputStream is = new FileOutputStream(file);
        is.write(data);
        is.flush();
        is.close();

        System.out.println("file saved in " + file);
        return file;
    }

    /**
     * 检查两个数组StartWith
     *
     * @param bytes
     * @param start
     * @param end
     * @param stringBytes
     * @return
     */
    public boolean byteCheckStartWith(byte[] bytes, int start, int end, byte[] stringBytes) {
        if (end - start < stringBytes.length) {
            return false;
        }
        for (int i = start, j = 0; i < end && j < stringBytes.length; i++, j++) {
            if (bytes[i] == stringBytes[j]) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查两个byte[]
     *
     * @param bytes
     * @param start
     * @param end
     * @param stringBytes
     * @param ignore
     * @return
     */
    public boolean byteCheckContains(byte[] bytes, int start, int end, byte[] stringBytes, char ignore) {
        if (end - start < stringBytes.length) {
            return false;
        }
        for (int i = start, j = 0; i < end && j < stringBytes.length; i++, j++) {
            if (bytes[i] == ignore) {
                j--;
                continue;
            }
            if (stringBytes[j] == ignore) {
                i--;
                continue;
            }
            if (bytes[i] == stringBytes[j]) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
