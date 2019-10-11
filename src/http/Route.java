package http;

import annotation.RequestMethod;

import java.lang.reflect.Method;

public class Route {
    public Object controller;//已经实例化的controller对象
    public String[] requestMethods;//请求方式？
    public String url;//这个程序只能用一个方法匹配，因为使用了HashMap
    public Method processMethod;
    public String mapping;
}