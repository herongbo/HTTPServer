package server;

import config.Config;
import http.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ByteReader {
    InputStream input;
    byte[] buffer;
    DataInputStream dis;

    public ByteReader(InputStream input, int bufferSize) {
        this.input = input;
        dis = new DataInputStream(input);
        buffer = new byte[bufferSize];
    }

    public ByteReader(InputStream input) {
        this(input, 1024);
    }

    int pre = 0;
    int temp = 0;

    public void read() throws IOException {
        long start = System.currentTimeMillis();

        List<String> headerList = new ArrayList<>();
        Request request = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int len = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.out.println("reuse buffer size  " + buffer.length);

        int size = 0;

        //测试缓冲区大小，每次读入的最大值是 65536 ？
        //现在测试到了1224的buffersize

//        while (input.available() > 0) {
//            len = input.read(buffer);
//            System.out.println("wait for upload " + len);
//            size += len;
//            baos.write(buffer, 0, len);
//        }

        while (dis.available()>0) {
            len = dis.read(buffer);
            System.out.println("wait for upload " + len + " " + dis.available());
            size += len;
            baos.write(buffer, 0, len);
        }


//        input.close();
        buffer = baos.toByteArray();
//        System.out.println("request size" + buffer.length);
//        System.out.println("mysize" + size);
        System.out.println(new String(buffer));
        System.out.println();
        System.out.println();

        System.out.println(buffer.length);

        System.out.println("timer log1 " + (System.currentTimeMillis() - start));
        int i = 0;
        for (i = 0; i < buffer.length; i++) {
            if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                String str = readString(buffer, pre, i);
//                System.err.println(str + ":x" + str.length());
                pre = i + 2;
                if (str.length() == 0) {
//                    System.out.println("nxx");
//                    headerList.forEach(System.out::println);
                    request = new Request(headerList);
                    System.out.println("header parse end");
//                    System.out.println(request);
                    System.err.println("header end");
                    break;
                }
                headerList.add(str);
            }
        }
        System.out.println("\n\nbody");
        temp = pre;
        System.out.println("timer log2 " + (System.currentTimeMillis() - start));

        String contentDesposition = null;
        String name = "";
        String fileName = "";
        for (; i < buffer.length; i++) {
            if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                String str = readString(buffer, temp, i);
//                String str = "";
                if (str.contains(request.boundary)) {
                    System.out.println(byteCheckContains(buffer, temp, i, request.boundary.getBytes(), '-'));
//                if (byteCheckContains(buffer, temp, i, request.boundary.getBytes(), '-')) {
                    System.err.println("boundary!!");
                    System.out.println(str);
                    System.out.println(request.boundary);

                    //现在需要判断是否读到了数据
                    //从boundary开始，读到了boundary意味着
                    //应该用文件名称判断文件是否为空
                    //空的文件名直接舍弃
                    //有名称的文件，保留
                    if (true || out.toByteArray().length > 0) {
                        System.err.println("process with file ");
                        //文件好像丢失了一些东西，检查哪里有问题
                        //将per到i的数据写写入缓冲区，保存到文件

                        //pre
                        if (temp - 2 - pre > 0) {
                            System.err.println(pre + " " + (temp - 2 - pre));
                            if (fileName != null) {
                                //文件类型
                                out.write(buffer, pre, temp - 2 - pre);
                                byte[] data = out.toByteArray();
                                out.reset();
                                File f = new File("C:\\Users\\JDUSER\\Desktop\\what\\" + fileName);
                                f.createNewFile();
                                FileOutputStream is = new FileOutputStream(f);
                                is.write(data);
                                is.flush();
                                is.close();
                                System.out.println("file saved in " + f);
                            } else {
                                //字符类型
                                System.out.println("data");
                                String data = new String(buffer, pre, temp - 2 - pre, Config.CHARSET);
                                System.out.println(data);
                            }

                        } else {
                            //webboundary第一次出现
                        }
                    }
                    temp = i + 2;
                    pre = i + 2;
                } else if (byteCheckStartWith(buffer, temp, i, "Content-Disposition: form-data;".getBytes())) {

                    str = new String(buffer, temp, i - temp, Config.CHARSET);
                    System.out.println("content data " + str);

                    String[] contents = str.split(";");
                    if (contents.length == 3) {//文件
                        contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                        name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                        fileName = contents[2].substring(contents[2].indexOf("\"") + 1, contents[2].length() - 1);
                    } else if (contents.length == 2) {//字符
                        contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                        name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                        fileName = null;
                    }

                    System.out.println(contentDesposition);
                    System.out.println(name);
                    System.out.println(fileName);

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

        System.err.println("end of program");
        System.err.println("timer log3 " + (System.currentTimeMillis() - start));
    }

    public String readString(byte[] bytes, int s, int e) {
        StringBuffer sb = new StringBuffer();
        for (int i = s; i < e; i++) {
            sb.append((char) bytes[i]);
        }
        return sb.toString();
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
//        System.out.println(end - start + " " + stringBytes.length);
//        System.out.println(new String(buffer, start, end - start));
//        System.out.println(new String(stringBytes));
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
//        System.out.println(end - start + " " + stringBytes.length);
//        System.out.println(new String(buffer, start, end - start));
//        System.out.println(new String(stringBytes));
        return true;
    }
}

/*

package server;

import config.Config;
import http.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ByteReader {
    InputStream input;
    byte[] buffer;

    public ByteReader(InputStream input, int bufferSize) {
        this.input = input;
        buffer = new byte[bufferSize];
    }

    public ByteReader(InputStream input) {
        this(input, 1024);
    }

    int pre = 0;
    int temp = 0;

    public void read() throws IOException {
        long start = System.currentTimeMillis();

        List<String> headerList = new ArrayList<>();
        Request request = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int len = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.out.println("reuse buffer size  " + buffer.length);

//        while ((len = input.read(buffer)) != -1) {
//            System.out.println(len + " " + input.available());
//            baos.write(buffer, 0, len);
////            if (len < buffer.length) {
////                break;
////            }
//        }


        int size = 0;

        //测试缓冲区大小，每次读入的最大值是 65536 ？
        //现在测试到了1224的buffersize

        while (input.available() > 0) {
            len = input.read(buffer);
//            System.out.println("wait for upload "+len);
            baos.write(buffer, 0, len);
//            System.out.println("available" + input.available());
//            if (len < buffer.length) {//                System.out.println("break!");
//                break;
//            }
        }

        input.close();
        buffer = baos.toByteArray();
//        System.out.println("request size" + buffer.length);
//        System.out.println("mysize" + size);
        System.out.println(new String(buffer));

        System.out.println(buffer.length);

        System.out.println("timer log1 " + (System.currentTimeMillis() - start));
        int i = 0;
        for (i = 0; i < buffer.length; i++) {
            if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                String str = readString(buffer, pre, i);
//                System.err.println(str + ":x" + str.length());
                pre = i + 2;
                if (str.length() == 0) {
//                    System.out.println("nxx");
//                    headerList.forEach(System.out::println);
                    request = new Request(headerList);
                    System.out.println("header parse end");
//                    System.out.println(request);
                    System.err.println("header end");
                    break;
                }
                headerList.add(str);
            }
        }
        System.out.println("\n\nbody");
        temp = pre;
        System.out.println("timer log2 " + (System.currentTimeMillis() - start));

        String contentDesposition = null;
        String name = "";
        String fileName = "";
        for (; i < buffer.length; i++) {
            if (buffer[i] == '\r' && buffer[i + 1] == '\n') {
                String str = readString(buffer, temp, i);
                if (str.contains(request.boundary)) {
                    System.err.println("boundary!!");
                    System.out.println(str);
                    System.out.println(request.boundary);

                    //现在需要判断是否读到了数据
                    //从boundary开始，读到了boundary意味着
                    //应该用文件名称判断文件是否为空
                    //空的文件名直接舍弃
                    //有名称的文件，保留
                    if (true || out.toByteArray().length > 0) {
                        System.err.println("process with file ");
                        //文件好像丢失了一些东西，检查哪里有问题
                        //将per到i的数据写写入缓冲区，保存到文件

                        //pre
                        if (temp - 2 - pre > 0) {
                            System.err.println(pre + " " + (temp - 2 - pre));
                            if (fileName != null) {
                                //文件类型
                                out.write(buffer, pre, temp - 2 - pre);
                                byte[] data = out.toByteArray();
                                out.reset();
                                File f = new File("C:\\Users\\JDUSER\\Desktop\\what\\" + new Date().hashCode() + fileName);
                                f.createNewFile();
                                FileOutputStream is = new FileOutputStream(f);
                                is.write(data);
                                is.flush();
                                is.close();
                                System.out.println("file saved in " + f);
                            } else {
                                //字符类型
                                System.out.println("data");
                                String data = new String(buffer, pre, temp - 2 - pre, Config.CHARSET);
                                System.out.println(data);
                            }

                        } else {
                            //webboundary第一次出现
                        }
                    }
                    temp = i + 2;
                    pre = i + 2;
                } else if (str.startsWith("Content-Disposition: form-data;")) {

                    str = new String(buffer, temp, i - temp, Config.CHARSET);
                    System.out.println("content data " + str);

                    String[] contents = str.split(";");
                    if (contents.length == 3) {//文件
                        contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                        name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                        fileName = contents[2].substring(contents[2].indexOf("\"") + 1, contents[2].length() - 1);
                    } else if (contents.length == 2) {//字符
                        contentDesposition = contents[0].substring(contents[0].indexOf(":") + 2);
                        name = contents[1].substring(contents[1].indexOf("\"") + 1, contents[1].length() - 1);
                        fileName = null;
                    }

                    System.out.println(contentDesposition);
                    System.out.println(name);
                    System.out.println(fileName);

                    temp = i + 2;
                    pre = i + 2;
                } else if (str.startsWith("Content-Type:")) {
                    pre = i + 4;
                    temp = i + 4;
                    i += 4;
                } else {
                    //读取到文件中间，移动temp指针
                    temp = i + 2;
                }
            }
        }

        System.err.println("end of program");
        System.err.println("timer log3 " + (System.currentTimeMillis() - start));
    }

    public String readString(byte[] bytes, int s, int e) {
        StringBuffer sb = new StringBuffer();
        for (int i = s; i < e; i++) {
            sb.append((char) bytes[i]);
        }
        return sb.toString();
    }

    public boolean byteCheckStartWith(byte[] bytes, int start, int end, byte[] stringBytes) {
        for (int i = 0, j = 0; i < bytes.length && j < stringBytes.length; i++, j++) {
            if (bytes[start + i] == stringBytes[j]) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
*/
