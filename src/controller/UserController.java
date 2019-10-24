package controller;

import annotation.Autowired;
import annotation.Controller;
import annotation.RequestMapping;
import annotation.RestController;
import entity.Book;
import http.HttpSession;
import http.Request;

import java.util.Date;

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
            String data = "登录成功";
            data += name;
            return data;
        }
    }

    @RequestMapping("/check.html")
    public String index(HttpSession session, String name, String password) {
        if (session.get("name") != null) {
            return session.get("name") + " 已经是登录状态";
        } else {
            if (name == null) {
                return "用户名为空";
            } else if (password == null) {
                return "密码为空";
            } else {
                String data = "登录成功";
                return data;
            }
        }
    }


    @RequestMapping("/html/login.java")
    public String error() {
        return new Date().toString();
    }

    @RequestMapping("/sessiontest")
    public String sessionTest(HttpSession session, String name) {

        System.out.println("name: " + session.get("name"));
        if (session.get("name") == null) {
            session.put("name", name);
            return session.getId() + "\n you have not login\n" + session.get("name");
        } else {
            return session.getId() + "\n hello" + session.get("name");
        }
    }

    @RequestMapping("/")
    public String test(Request request, String name, String password) {
        return this.index(name, password, null);
    }

    @RequestMapping("/index.html")
    public String index(String name, String password, HttpSession session) {
        //最多能提供request 表单参数 和session 以及 stream
        String data = "talk is cheap and show me the code\n";
        data += new Date().toString() + "\n";
        data += name + "\n";
        data += password + "\n";
        return data;
    }
}
