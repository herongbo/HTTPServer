package controller;


import annotation.Controller;
import annotation.RequestMapping;

@Controller
public class PageController {

    @RequestMapping("/*")
    public String error() {
        return ("poem");
    }

    @RequestMapping("/index.html")
    public String index(String name,String password) {
        //最多能提供request 表单参数 和session 以及 stream
        System.out.println("talk is cheap and show me the code");
        return ("poem");
    }
}
