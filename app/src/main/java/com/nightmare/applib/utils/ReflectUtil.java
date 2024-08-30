package com.nightmare.applib.utils;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/// 反射工具类
public class ReflectUtil {

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
    }

    static HashMap<String, String> map = new HashMap<>();

    public static void listAllObject(Object object) {
        listAllObject(object.getClass());
    }

    public static void listAllObject(Class clazz) {
        map.put("class java.lang.String", "String");
        map.put("java.util.List","List");
        try {
            print("Class " + clazz.getName());
            // 反射属性字段
            Field[] fields = clazz.getDeclaredFields();

            // 反射方法字段
            Method[] methods = clazz.getDeclaredMethods();

            // 反射构造器
            Constructor[] constuctors = clazz.getDeclaredConstructors();
            print("CONSTUCTOR========");
            for (Constructor c : constuctors) {
                System.out.print((char) 0x1b);
                System.out.print("[34m");
                System.out.flush();
                print("NAME:" + c.getName());
            }
            System.out.print("\n");
            print("FIELD========");
            for (Field f : fields) {
                boolean isStatic = Modifier.isStatic(f.getModifiers());
                System.out.print((char) 0x1b + "[36m");
                if(isStatic){
                    System.out.print("static ");
                }
                System.out.print(f.getType());
                System.out.print((char) 0x1b);
//                System.out.println("[0;32m NAME:" + (char) 0x1b + "[31m" + f.getName() + (char) 0x1b + "[0m");
                System.out.println("[0;32m " + (char) 0x1b + "[32m" + f.getName() + (char) 0x1b + "[0m");
                System.out.flush();
            }
            System.out.print("\n");
            print("METHOD========");
            for (Method m : methods) {
                boolean isStatic = Modifier.isStatic(m.getModifiers());
                String returnType = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    returnType = m.getReturnType().getTypeName();
                }
                if(map.containsKey(returnType)){
                    returnType = map.get(returnType);
                }
//                returnType = returnType.replace("class ", "");
                if(isStatic){
                    System.out.print("static ");
                }
                System.out.print((char) 0x1b + "[36m" + returnType + " ");
                System.out.print((char) 0x1b);
                System.out.print("[33m" + m.getName());
                System.out.print("(" + (char) 0x1b + "[32m");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    for (int i = 0; i < m.getParameterCount(); i++) {
                        String type = m.getParameters()[i].getParameterizedType().toString();
                        if (map.containsKey(type)) {
                            type = map.get(type);
                        } else {
                            type = type.replace("class ", "");
                        }
                        System.out.print(type + " " + m.getParameters()[i].getName());
                        if (i != m.getParameterCount() - 1) {
                            System.out.print(",");
                        }
                    }
                }
                System.out.print((char) 0x1b + "[0m)\n");
                System.out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
