package annotation;

import http.Route;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * AnnotationReader同时也是bean的管理工具
 *
 * @author JDUSER
 */
public class AnnotationReader {
    /*
    获取到package下面的class，可能需要递归操作

    获取注解，class和method的
    @RequestMapping注解
    @Controller
    @AutoWired
    @Modify

     */

    // 维护两个映射表,哈希直接映射到url
    public Map<String, Route> routeMap = new HashMap<>();
    // List映射到通配符
    public List<Route> routeList = new ArrayList<>();

    List<String> packageList = Arrays.asList("controller", "entity");

    public AnnotationReader() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //扫描package获取所有的class
        List<String> classList = new ArrayList<>();

        for (String packageName : packageList) {
            getClassName(packageName, classList);
        }

        //处理每一个class文件
        for (String className : classList) {
            System.out.println(className);
            Class clazz = Class.forName(className);
            Method[] methods = clazz.getMethods();
            Field[] fields = clazz.getFields();
            Object object = null;

//            //设计一个映射方法，还有不定长的参数如何处理
//            //根据url获取到对象和方法，然后invoke Method

            if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
                object = clazz.getDeclaredConstructor().newInstance();
            }

            //Autowared？这里必须同时使用AutoWared和Qualifier，
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class) && field.isAnnotationPresent(Qualifier.class)) {
                    Qualifier qualifier = field.getAnnotation(Qualifier.class);
                    String name = qualifier.value();
                    field.set(object, Class.forName(name));
                }
            }

            for (Method method : methods) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    String url = requestMapping.value();
                    {
                        Route route = new Route();
                        route.url = url;
                        route.controller = object;
                        route.processMethod = method;
                        if (clazz.isAnnotationPresent(RestController.class)) {
                            route.mapping = "RestController";
                        } else if (clazz.isAnnotationPresent(Controller.class)) {
                            route.mapping = "Controller";
                        }
                        if (url.endsWith("*")) {
                            routeList.add(route);
                        } else {
                            routeMap.put(url, route);
                        }
                    }
                }
            }
        }
    }

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


