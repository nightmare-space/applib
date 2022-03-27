package com.nightmare.applib_util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ReflectUtil {

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
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
                System.out.print((char) 0x1b + "[33mMETHOD NAME:");
                System.out.print((char) 0x1b + "[31m");
                System.out.print(m.getName());
                System.out.print((char) 0x1b);
                System.out.print("[0;33m Parameter:" + (char) 0x1b + "[31m" + Arrays.toString(m.getParameters()) + (char) 0x1b + "[0m");
                System.out.println((char) 0x1b + "[33m RETURE TYPE:" + (char) 0x1b + "[31m" + m.getGenericReturnType() + (char) 0x1b + "[0m");
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
