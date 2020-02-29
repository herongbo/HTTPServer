# 正在完成中的webServer

##计划实现的功能
- [x] BIO工作模式
- [ ] NIO工作模式
- [x] 请求接收
- [x] 表单解析
- [ ] 文件上传
- [x] 注解
- [x] Session管理
- [x] 表单处理
#说明
Config.static配置静态资源路径
@RestController注解编写动态页面
配置文件为config.properties
默认端口为8080

运行后访问http://localhost:8080/hello.html
```java

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
}

```
![demo](/img/page1.png)
![demo](/img/page2.png)
