package controller;


import annotation.Autowired;
import annotation.Controller;
import annotation.RequestMapping;
import entity.Book;

@Controller
public class PageController {

    @Autowired
    Book book;

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("error")
    public String error() {
        int a = 2 / 0;
        return "login";
    }
}
