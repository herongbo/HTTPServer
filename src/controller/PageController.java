package controller;


import annotation.Autowired;
import annotation.Controller;
import annotation.RequestMapping;
import entity.Book;

@Controller
public class PageController {

    @Autowired
    Book book;

    @RequestMapping("/git")
    public String error() {
        return ("hello");
    }
}
