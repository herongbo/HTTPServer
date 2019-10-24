# 正在完成中的webServer

##计划实现的功能
- [x] BIO工作模式
- [ ] NIO工作模式
- [x] 请求接收
- [x] 表单解析
- [ ] 文件上传
- [ ] 注解
- [ ] Session管理
- [ ] 表单处理
#说明
Config.static配置静态资源路径
@RestController注解编写动态页面
```java

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
}

```