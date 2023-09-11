package com.kiin.springmvc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiin.springmvc.annotation.RequestMapping;
import com.kiin.springmvc.annotation.RequestParam;
import com.kiin.springmvc.annotation.ResponseBody;
import com.kiin.springmvc.context.KiinWebApplicationContext;
import com.kiin.springmvc.handle.KiinHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author kiin
 * 前端控制器
 */
public class KiinDispatcherServlet extends HttpServlet {

    // 保存 URL 和控制器的映射
    private List<KiinHandler> handlerList = new ArrayList<>();

    KiinWebApplicationContext applicationContext = null;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        String configLocation = servletConfig.getInitParameter("contextConfigLocation");
        applicationContext = new KiinWebApplicationContext(configLocation.split(":")[1]);
        applicationContext.init();
        // 完成 URL 和控制器方法的映射
        initHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        executeDispatch(req, resp);
    }

    private void initHandlerMapping() {
        // ioc容器为 null
        if (applicationContext.singletonObjects.isEmpty()) {
            return;
        }
        // 遍历 ioc 容器，然后进行 URL 映射
        applicationContext.singletonObjects.entrySet().forEach(entry -> {
            Class<?> clazz = entry.getValue().getClass();
            // 取出所有方法
            Method[] declaredMethod = clazz.getDeclaredMethods();
            // 遍历方法
            for (Method method : declaredMethod) {
                // 判断方法是否被 requestMapping 注解修饰
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    // 获得RequestMapping注解设置的 value @RequestMapping("/list/monster")
                    String url = annotation.value();
                    // 映射关系对象
                    KiinHandler handler = new KiinHandler(url, entry.getValue(), method);
                    handlerList.add(handler);
                }
            }
        });

        System.out.println("handlerList :: " + handlerList);
    }

    private KiinHandler getHandler(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        for (KiinHandler handler : handlerList) {
            if (handler.getUrl().equals(requestURI)) {
                return handler;
            }
        }
        return null;
    }

    private void executeDispatch(HttpServletRequest request, HttpServletResponse response) {
        KiinHandler handler = getHandler(request);
        PrintWriter writer = null;
        response.setContentType("text/html;charset=UTF-8");
        try {
            writer = response.getWriter();
            // 为空，说明uri 没有匹配上，handlerList中没有相应的路径
            if (null == handler) {
                writer.write("<h1>404 NOT FOUND</h1>");
            } else {
                // 得到目标方法的所有形参信息，不是具体对象
                Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
                // 创建一个参数数组，反射调用方法时会用到
                Object[] params = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    if ("HttpServletRequest".equals(parameterType.getSimpleName())) {
                        params[i] = request;
                    } else if ("HttpServletResponse".equals(parameterType.getSimpleName())) {
                        params[i] = response;
                    }
                }
                request.setCharacterEncoding("UTF-8");
                // value 是数组是因为会有 checkbox 情况的出现，一个 name 对应多个 value
                Map<String, String[]> parameterMap = request.getParameterMap();
                parameterMap.entrySet().forEach(entry -> {
                    // 对应请求的参数名 ?name=jack&age=11
                    String name = entry.getKey();
                    // ?name=jack&age=11，
                    String value = entry.getValue()[0];
                    int requestParameterIndex = getRequestParameterIndex(handler.getMethod(), name);
                    if (requestParameterIndex != -1) {
                        params[requestParameterIndex] = value;
                    } else {
                        // 如果为-1，说明没有找到@RequestParam注解对应的参数
                        List<String> parameterNames = getParameterNames(handler.getMethod());
                        for (int i = 0; i < parameterNames.size(); i++) {
                            if (name.equals(parameterNames.get(i))) {
                                params[i] = value;
                                break;
                            }
                        }
                    }
                });
                // 匹配成功
                Method method = handler.getMethod();
                // 方法返回结果，return "forward:/login_ok.jsp";
                Object result = method.invoke(handler.getController(), params);
                // 判断返回结果是不是字符串类型
                if (result instanceof String) {
                    String viewName = (String) result;
                    if (viewName.contains(":")) {
                        // forward: 或者 redirect
                        String viewType = viewName.split(":")[0];
                        // /login_ok.jsp
                        String viewPage = viewName.split(":")[1];
                        if ("forward".equals(viewType)) {
                            request.getRequestDispatcher(viewPage).forward(request, response);
                        } else if ("redirect".equals(viewType)) {
                            response.sendRedirect(request.getServletContext().getContextPath() + viewPage);
                        }
                    } else {
                        // 默认情况进行请求转发
                        request.getRequestDispatcher(viewName).forward(request, response);
                    }
                } else if (result instanceof ArrayList) {
                    Method jsonMethod = handler.getMethod();
                    if (jsonMethod.isAnnotationPresent(ResponseBody.class)) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonStr = objectMapper.writeValueAsString(result);
                        writer.write(jsonStr);
                        writer.flush();
                        writer.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param method 反射执行的目标方法
     * @param name   形参名
     * @return
     */
    private int getRequestParameterIndex(Method method, String name) {
        // 得到 method 中的所有形参
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // 取出当前形参
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                // @RequestParam("name") String name
                String value = requestParam.value();
                if (value.equals(name)) {
                    // 对应目标方法的形参位置
                    return i;
                }
            }
        }
        return -1;
    }

    private List<String> getParameterNames(Method method) {
        List<String> list = new ArrayList<>();
        // 获取方法里的形参的形参名
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            list.add(parameter.getName());
        }
        System.out.println(list);
        return list;
    }
}
