package com.kiin.springmvc.context;

import com.kiin.springmvc.annotation.AutoWired;
import com.kiin.springmvc.annotation.Controller;
import com.kiin.springmvc.annotation.Service;
import com.kiin.springmvc.utils.XMLParser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KiinWebApplicationContext {

    private List<String> classFullPathList = new ArrayList<>();

    public Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private String configLocation;

    public KiinWebApplicationContext() {
    }

    public KiinWebApplicationContext(String configLocation) {
        this.configLocation = configLocation;
    }

    public void init() {
        String basePackage = XMLParser.parserXml(configLocation);
        String[] basePackages = basePackage.split(",");
        for (String s : basePackages) {
            scanPackage(s);
        }
        // 将扫描到的被 Controller注解修饰的类反射到 ioc 容器中
        executeInstance();
        executeAutowired();
        System.out.println("");
    }

    public void scanPackage(String pack) {
        URL url = this.getClass().getClassLoader().getResource("/" + pack.replaceAll("\\.", "/"));
        String file = url.getFile();
        File[] files = new File(file).listFiles();
        for (File f : files) {
            // 如果是一个目录 需要递归扫描
            if (f.isDirectory()) {
                scanPackage(pack + "." + f.getName());
            } else {
                // 类的全路径 com.kiin.controller.MonsterController
                String className = pack + "." + f.getName().replaceAll(".class", "");
                classFullPathList.add(className);
            }
        }
    }

    /**
     * 创建实例
     */
    public void executeInstance() {
        if (classFullPathList.size() == 0) {
            return;
        }
        // 遍历
        for (String classFullPath : classFullPathList) {
            try {
                // 通过反射获取类
                Class<?> clazz = Class.forName(classFullPath);
                // 判断类是否被 controller修饰
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // 实例化对象
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    // 放进 ioc容器(singletonObject)
                    String simpleName = clazz.getSimpleName();
                    // 类的首字母小写作为 key ，实例对象为 value
                    singletonObjects.put(StringUtils.uncapitalize(simpleName), instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // 得到 service 注解
                    Service serviceAnnotation = clazz.getAnnotation(Service.class);
                    // 得到注解配置的 value 属性
                    String beanName = serviceAnnotation.value();
                    // 如果为空，说明没有指定，那就用接口做默认 beanName
                    if ("".equals(beanName)) {
                        // 反射得到的类所实现的所有接口
                        Class<?>[] interfaces = clazz.getInterfaces();
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        for (Class<?> anInterface : interfaces) {
                            // 接口名首字母小写
                            String interfaceName = StringUtils.uncapitalize(anInterface.getSimpleName());
                            singletonObjects.put(interfaceName, instance);
                        }
                        // 通过反射得到的类的类名
                        String simpleName = clazz.getSimpleName();
                        singletonObjects.put(StringUtils.uncapitalize(simpleName), instance);

                        singletonObjects.entrySet().forEach((entry) -> {
                            System.out.println("entry :: " + entry);
                        });
                        // 指定了 value
                    } else {
                        singletonObjects.put(beanName, clazz.getDeclaredConstructor().newInstance());

                    }
                }
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 完成属性的自动装配
     */
    private void executeAutowired() {
        if (singletonObjects.isEmpty()) {
            return;
        }

        singletonObjects.entrySet().forEach(entry -> {
            // 容器里的 bean
            String key = entry.getKey();
            Object bean = entry.getValue();
            // 获取 bean 的所有属性
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            // 遍历所有属性  private MonsterService monsterService;
            for (Field declaredField : declaredFields) {
                // 判断属性是否被 AutoWired 修饰
                if (declaredField.isAnnotationPresent(AutoWired.class)) {
                    // 得到 AutoWired 这个注解，用于判断该注解有没有自定义 value 属性
                    AutoWired annotation = declaredField.getAnnotation(AutoWired.class);
                    // 获取注解的 value
                    String beanName = annotation.value();
                    // 为空，说明没有自定义 value
                    if ("".equals(beanName)) {
                        // 获取属性的类型 private MonsterService monsterService  类型为MonsterService
                        Class<?> declaredFieldType = declaredField.getType();
                        // 类型的名字 MonsterService
                        String simpleName = declaredFieldType.getSimpleName();
                        // 将首字母小写  monsterService
                        beanName = StringUtils.uncapitalize(simpleName);

                    }
                    // 不为空，说明有自定义 beanName
                    // 先判断对应 beanName 的 bean 是否为空
                    if (null == singletonObjects.get(beanName)) {
                        throw new RuntimeException("容器没有对应的 bean");
                    }
                    // 防止属性为 private，暴力破解
                    declaredField.setAccessible(true);
                    // 装配属性
                    try {
                            /*
                              bean 和singletonObjects.get(beanName)不是同一个
                              假如遍历时，从 entry 里获取的 bean 是 controller,singletonObjects.get(beanName)就会是 service，beanName是从
                              注解里 value 属性获取的
                             */
                        declaredField.set(bean, singletonObjects.get(beanName));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
