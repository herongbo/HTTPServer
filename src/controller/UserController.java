package controller;

import annotation.Autowired;
import annotation.RequestMapping;
import annotation.RestController;
import entity.Book;
import http.HttpSession;
import http.Request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    Book book;

    @RequestMapping("/api/logincheck")
    public String loginCheck(HttpSession session, String name, String password) {
        System.out.println("name" + name);
        System.out.println("password" + password);

        if (name == null) {
            return "用户名为空";
        } else if (password == null) {
            return "密码为空";
        } else {
            session.put("name", name);
            return "success";
        }
    }

    @RequestMapping("/api/time")
    public String time() {
        return new Date().toString();
    }

    @RequestMapping("/sessiontest")
    public String sessionTest(HttpSession session, String name) {
        System.out.println("name: " + session.get("name"));
        if (session.get("name") == null) {
            session.put("name", name);
            return session.getId() + "<br> you have not login<br>" + session.get("name");
        } else {
            return session.getId() + "<br> hello<br>" + session.get("name");
        }
    }


    @RequestMapping("/")
    public String test(Request request, String name, String password) {
        return this.index(name, password, null);
    }

    @RequestMapping("error")
    public String error() {
        int a = 2 / 0;
        return "login";
    }

    @RequestMapping("/index.html")
    public String index(String name, String password, HttpSession session) {
        //最多能提供request 表单参数 和session 以及 stream
        String data = "login success \n";
        data += new Date().toString() + "\n";
        data += session.get("name") + "\n";
        data += session.get("password") + "\n";
        return data;
    }
}
