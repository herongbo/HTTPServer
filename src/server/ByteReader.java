package server;

import config.Config;
import http.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteReader {
    InputStream input;
    byte[] buffer;

    public ByteReader(InputStream input, int bufferSize) {
        this.input = input;
        buffer = new byte[bufferSize];
    }

    //设置大缓冲区可能会等待
    public ByteReader(InputStream input) {
        this(input, 1024 * 64);
    }

    public void read() throws IOException {
        int pre = 0;
        int temp = 0;

        long start = System.currentTimeMillis();
        Request request = null;

        List<String> headerList = new ArrayList<>();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream fos = new ByteArrayOutputStream();

        long size = 0;
        int len = 0;

        while (true) {
            len = input.read(buffer);
            bos.write(buffer, 0, len);
            size += len;

            if (input.available() == 0 && size > 1024 * 16) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (input.available() == 0) {
                    break;
                }
            } else if (input.available() == 0 && size < 1024 * 16) {
                break;
            }
        }

        buffer = bos.toByteArray();

        System.out.println("timer log1 " + (System.currentTimeMillis() - start));
        int i;
        for (i = 0; i < buffer.length; i++) {
            if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                String str = new String(buffer, pre, i - pre);
                if (i - pre <= 1) {
                    System.out.println("yes");
                    request = new Request(headerList);
                    System.out.println("header parse end");
                    break;
                }
                pre = i + 2;
                headerList.add(str);
            }
        }

        //重置指针
        temp = pre;
        System.out.println("timer log2 " + (System.currentTimeMillis() - start));

        String contentDesposition = null;
        String name = null;
        String fileName = null;
        System.out.println(request.boundary);
        System.out.println(request.content_type);

        if (request.content_type.equals("multipart/form-data")) {
            for (; i < buffer.length; i++) {
                if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                    if (byteCheckContains(buffer, temp, i, request.boundary.getBytes(), '-')) {
                        System.err.println("processing with file ");
                        //判断是否是第一个boundary
                        if (temp - 2 - pre > 0) {
                            if (fileName != null) {
                                //文件类型
                                fos.write(buffer, pre, temp - 2 - pre);
                                byte[] data = fos.toByteArray();
                                fos.reset();
                                File f = new File("C:\\Users\\JDUSER\\Desktop\\what\\" + fileName);
                                f.createNewFile();
                                FileOutputStream is = new FileOutputStream(f);
                                is.write(data);
                                is.flush();
                                is.close();
                                System.out.println("file saved in " + f);
                            } else {
                                //字符类型
                                String data = new String(buffer, pre, temp - 2 - pre, Config.CHARSET);
                                System.out.println(data);
                            }

                        }
                        temp = i + 2;
                        pre = i + 2;
                    } else if (byteCheckStartWith(buffer, temp, i, "Content-Disposition: form-data;".getBytes())) {

                        String str = new String(buffer, temp, i - temp, Config.CHARSET);
                        System.out.println("content data " + str);

                        String[] contents = str.split(";");
                        if (contents.length == 3) {
                            //文件
                            contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                            name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                            fileName = contents[2].substring(contents[2].indexOf("\"") + 1, contents[2].length() - 1);
                        } else if (contents.length == 2) {
                            //字符
                            contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                            name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                            fileName = null;
                        }
                        temp = i + 2;
                        pre = i + 2;
                    } else if (byteCheckStartWith(buffer, temp, i, "Content-Type:".getBytes())) {
                        pre = i + 4;
                        temp = i + 4;
                        i += 4;
                    } else {
                        //读取到文件中间，移动temp指针
                        temp = i + 2;
                    }
                }
            }
        } else {
            //普通的表单
            String str = new String(buffer, pre, buffer.length - pre);
            String[] data = str.split("&");
            for (String s : data) {
                String key = s.substring(0, s.indexOf("="));
                String value = s.substring(s.indexOf("=") + 1);
                request.paramater.put(key, value);
            }
            Arrays.stream(data).forEach(System.out::println);
        }

        if (!request.content_type.equals("multipart/form-data") && request.method.equals("GET")) {
            String str = request.url.substring(request.url.indexOf("?"));
            String[] data = str.split("&");
            for (String s : data) {
                String key = s.substring(0, s.indexOf("="));
                String value = s.substring(s.indexOf("=") + 1);
                request.paramater.put(key, value);
            }
            Arrays.stream(data).forEach(System.out::println);
        } else {

        }
        System.err.println("timer log3 " + (System.currentTimeMillis() - start));
    }


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
