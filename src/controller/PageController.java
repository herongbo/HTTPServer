package controller;


import annotation.Autowired;
import annotation.Controller;
import annotation.RequestMapping;
import annotation.RestController;
import entity.Book;
import http.Request;

import javax.xml.crypto.Data;
import java.util.Date;

@RestController
public class PageController {

    @Autowired
    Book book;

    @RequestMapping("/123456")
    public String error() {
        return ("poem");
    }


    @RequestMapping("ssss")
    public String ssss(Request request, String name, String password) {
        return request.url;
    }

    @RequestMapping("/index.html")
    public String index(String name, String password) {
        //最多能提供request 表单参数 和session 以及 stream
        String data = "talk is cheap and show me the code\n";
        data += new Date().toString() + "\n";
        data += name + "\n";
        data += password + "\n";
        return data;
    }
}
