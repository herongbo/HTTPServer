package controller;

import annotation.Autowired;
import annotation.Controller;
import annotation.RequestMapping;
import entity.Book;

@Controller
public class PageController {

    @Autowired
    Book book;

    // 登录界面1
    @RequestMapping("/login")
    public String login() {
        return "index";
    }

    // 登录界面2
    @RequestMapping("/login2")
    public String log(){
        return "login";
    }


//    @RequestMapping("error")
//    public String error() {
//        int a = 2 / 0;
//        return "login";
//    }


}
