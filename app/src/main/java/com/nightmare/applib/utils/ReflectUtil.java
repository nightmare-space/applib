package com.nightmare.applib.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// 反射工具类
public class ReflectUtil {

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
    }

    public static void listAllObject(Object object) {
        listAllObject(object.getClass());
    }

    public static void listAllObject(Class clazz) {
        try {
            print("Class " + clazz.getName());
            // 反射属性字段
            Field[] fields = clazz.getDeclaredFields();

            // 反射方法字段
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();

            // 反射构造器
            Constructor[] constuctors = clazz.getDeclaredConstructors();

            print("FIELD========");
            for (Field f : fields) {
                System.out.print((char) 0x1b + "[32mTYPE:");
                System.out.print((char) 0x1b + "[31m");
                System.out.print(f.getType());
                System.out.print((char) 0x1b);
                System.out.println("[0;32m NAME:" + (char) 0x1b + "[31m" + f.getName() + (char) 0x1b + "[0m");
                System.out.flush();
            }

            print("METHOD========");
            for (java.lang.reflect.Method m : methods) {
                System.out.print((char) 0x1b + "[35m" + m.getGenericReturnType() + " ");
                System.out.print((char) 0x1b);
                System.out.print("[34m" + m.getName());
                List<Parameter> parameters = new ArrayList<>();
                System.out.print("(" + (char) 0x1b + "[33m");
                for (int i = 0; i < m.getParameterCount(); i++) {
                    System.out.print(m.getParameters()[i].getParameterizedType() + " " + m.getParameters()[i].getName() + ",");
                }
                System.out.print((char) 0x1b + "[0m)\n");
                System.out.flush();
            }

            print("CONSTUCTOR========");
            for (Constructor c : constuctors) {
                System.out.print((char) 0x1b);
                System.out.print("[34m");
                System.out.flush();
                print("NAME:" + c.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
