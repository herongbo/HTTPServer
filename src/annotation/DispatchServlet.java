package annotation;

import config.Config;
import http.Request;
import http.Response;

import java.awt.geom.AffineTransform;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

/**
 * 管理bean
 * 请求分发
 */
public class DispatchServlet {

    private List<String> classNameList = new ArrayList<>();

    /**
     * IOC 容器
     */
    private Map<String, Object> iocMap = new HashMap<>();

    /**
     * route map 映射到普通url
     * route List映射到通配符的url
     */
    public Map<String, Method> handlerMapping = new HashMap<>();

    /**
     * debug 测试数据
     */
    List<String> packageList = Arrays.asList("controller", "entity");

    public DispatchServlet() {
        init();
    }

    public void init() {
        // 1、加载配置文件
        doLoadConfig();

        // 2、扫描需要管理的类
        doScanner("entity");
        doScanner("controller");

        // 3、初始化IOC容器，将所有相关的类保存到IOC容器中
        doInstance();

        // 4、依赖注入
        doAutowired();

        // 5、初始化 HandlerMapping
        initHandlerMapping();

        // 6、打印数据
        doTestPrintData();
    }

    /**
     * 9 检查Controller
     */
    public void controllerHandlerAdapter(Request request, Response response) {
        Method method = handlerMapping.get(request.url);

        // 获取方法的相关参数
        String className = method.getDeclaringClass().getName();
        Object object = iocMap.get(className);
        Class<?> returnType = method.getReturnType();
        Parameter[] parameters = method.getParameters();

        // 根据参数类型生成参数
        Object[] parametersValue = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            // 判断是否是request
            if (parameter.getType() == Request.class) {
                parametersValue[i] = request;
            } else {
                //当成表单内容解析
                String data = request.paramater.get(parameter.getName());
                parametersValue[i] = data;
            }
        }

        // 通过反射调用方法
        System.out.println(className);
        System.out.println(object);
        String data = null;
        try {
            data = (String) method.invoke(object, parametersValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        // 数据写入到流中
        response.httpSuccess();
        try {
            response.getWriter().write(data);
            response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 8 映射到静态资源文件
     */
    public void staticHandlerAdapter(Request request, Response response) throws IOException {

        String basePath = Thread.currentThread().getContextClassLoader().getResource(Config.STATIC).getFile().substring(1);
        File file = new File(basePath + request.url);

        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int i = 0;
            response.httpSuccess();
            while ((i = fis.read(buffer)) > 1) {
                response.getOutputStream().write(buffer, 0, i);
            }
            response.getOutputStream().flush();
        }
    }

    /**
     * 7 Handler Mapping 请求分发
     */
    public void handlerAdapter(Request request, Response response) throws IOException {
        System.out.println("[info 7] handlerAdapter working");


        String basePath = Thread.currentThread().getContextClassLoader().getResource(Config.STATIC).getFile().substring(1);
        if (handlerMapping.containsKey(request.url)) {
            controllerHandlerAdapter(request, response);
        } else if (new File(basePath + request.url).exists()) {
            staticHandlerAdapter(request, response);
        } else {
            response.http404();
        }
        System.out.println("[info 7] handlerAdapter end ");
    }

    /**
     * 6、打印数据
     */
    private void doTestPrintData() {

        System.out.println("[INFO-6]----data------------------------");

        System.out.println("contextConfig.propertyNames()-->");

        System.out.println("[classNameList]-->");
        for (String str : classNameList) {
            System.out.println(str);
        }

        System.out.println("[iocMap]-->");
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            System.out.println(entry);
        }

        System.out.println("[handlerMapping]-->");
        for (Map.Entry<String, Method> entry : handlerMapping.entrySet()) {
            System.out.println(entry);
        }

        System.out.println("[INFO-6]----done-----------------------");

        System.out.println("====启动成功====");
    }

    /**
     * 5、初始化HandlerMapping
     */
    private void initHandlerMapping() {

        if (iocMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(RestController.class)) {
                continue;
            }

            String baseUrl = "";

            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }

                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");

                handlerMapping.put(url, method);

                System.out.println("[INFO-5] handlerMapping put {" + url + "} - {" + method + "}.");

            }
        }
    }

    /**
     * 4、依赖注入
     */
    private void doAutowired() {
        if (iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {

            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                // 判断字段是否使用Autowired注解
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                System.out.println("[INFO-4] Existence Autowired.");

                // 获取注解对应的类
                // 分成两种情况，如果有qualifier注解，把注解中的内容当成beanname
                // 否则，把类名当成beanName ， 然后从Map中找到指定的类
                String beanName;
                if (field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    beanName = qualifier.value();
                } else {
                    System.out.println("[INFO] field is not present by Qualifier ");
                    beanName = field.getType().getName();
                }
                System.out.println("[INFO] bean name: " + beanName);

                //只要加了注解，都要加载，不管是private还是public
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), iocMap.get(beanName));

                    System.out.println("[INFO-4] field set {" + entry.getValue() + "} - {" + iocMap.get(beanName) + "}.");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 3、初始化IOC容器
     */
    private void doInstance() {
        if (classNameList.isEmpty()) {
            return;
        }
        try {
            for (String className : classNameList) {

                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                    // 短类名必须重构
                    String simpleBeanName = clazz.getSimpleName().toLowerCase();
                    String beanName = clazz.getName();
                    Object instance = clazz.getDeclaredConstructor().newInstance();

                    // 保存到ioc容器？两个类持有一个对象
                    iocMap.put(beanName, instance);
                    iocMap.put(simpleBeanName, instance);
                    System.out.println("[INFO-3] {" + beanName + "} has been saved in iocMap.");

                } else if (clazz.isAnnotationPresent(Service.class)) {

                    String beanName = clazz.getSimpleName().toLowerCase();

                    // 检查是否包含自定义名称
                    Service service = clazz.getAnnotation(Service.class);
                    if (!"".equals(service.value())) {
                        beanName = service.value();
                    }

                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    iocMap.put(beanName, instance);
                    System.out.println("[INFO-3] {" + beanName + "} has been saved in iocMap.");

                    // 寻找类实现的接口(整合@qualifier
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (iocMap.containsKey(i.getName())) {
                            // 接口有多个实现类
                            throw new Exception("The Bean Name Is Exist.");
                        }

                        iocMap.put(i.getName(), instance);
                        System.out.println("[INFO-3] {" + i.getName() + "} has been saved in iocMap.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 2、扫描相关的类
     * 对应的应该是componentScan注解，
     *
     * @param scanPackage:需要扫描的包名
     */
    private void doScanner(String scanPackage) {
        URL resourcePath = Thread.currentThread().getContextClassLoader().getResource(scanPackage);

        if (resourcePath == null) {
            return;
        }

        File packageFile = new File(resourcePath.getFile());

        for (File file : packageFile.listFiles()) {
            if (file.isDirectory()) {

                System.out.println("[INFO-2] {" + file.getName() + "} is a directory.");

                // 递归扫描目录
                doScanner(resourcePath + "/" + file.getName());

            } else {

                if (!file.getName().endsWith(".class")) {
                    System.out.println("[INFO-2] {" + file.getName() + "} is not a class file.");
                    continue;
                }

                String className = scanPackage.replaceAll("/", ".") + "." + file.getName();

                className = className.substring(0, className.length() - 6);

                classNameList.add(className);

                System.out.println("[INFO-2] {" + className + "} has been saved in classNameList.");

            }
        }
    }

    /**
     * 1、加载配置文件
     */
    private void doLoadConfig() {
    }

//    public DispatchServlet() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        //扫描package获取所有的class
//        List<String> classList = new ArrayList<>();
//
//        for (String packageName : packageList) {
//            getClassName(packageName, classList);
//        }
//
//        //处理每一个class文件
//        for (String className : classList) {
//            System.out.println(className);
//            Class clazz = Class.forName(className);
//            Method[] methods = clazz.getMethods();
//            Field[] fields = clazz.getFields();
//            Object object = null;
//
////            //设计一个映射方法，还有不定长的参数如何处理
////            //根据url获取到对象和方法，然后invoke Method
//
//            if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
//                object = clazz.getDeclaredConstructor().newInstance();
//            }
//
//            //Autowared？这里必须同时使用AutoWared和Qualifier，
//            for (Field field : fields) {
//                if (field.isAnnotationPresent(Autowared.class) && field.isAnnotationPresent(Qualifier.class)) {
//                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
//                    String name = qualifier.value();
//                    field.set(object, Class.forName(name));
//                }
//            }
//
//            for (Method method : methods) {
//                if (method.isAnnotationPresent(RequestMapping.class)) {
//                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
//                    String url = requestMapping.value();
//                    {
//                        Route route = new Route();
//                        route.url = url;
//                        route.controller = object;
//                        route.processMethod = method;
//                        if (clazz.isAnnotationPresent(RestController.class)) {
//                            route.mapping = "RestController";
//                        } else if (clazz.isAnnotationPresent(Controller.class)) {
//                            route.mapping = "Controller";
//                        }
//                        if (url.endsWith("*")) {
//                            routeList.add(route);
//                        } else {
//                            routeMap.put(url, route);
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * 获取包名下的所有类名（递归操作）
     *
     * @param packageName
     * @param classList
     * @throws IOException
     */
    public void getClassName(String packageName, List<String> classList) throws IOException {
        String packageRealPath = Thread.currentThread().getContextClassLoader().getResource(packageName).getFile();
        File packageFile = new File(packageRealPath);

        for (File file : packageFile.listFiles()) {
            if (file.isDirectory()) {
                getClassName(packageName + "/" + file.getName(), classList);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName.replaceAll("/", ".") + "." + file.getName();
                className = className.substring(0, className.length() - 6);
                classList.add(className);
            }
        }
    }
}


