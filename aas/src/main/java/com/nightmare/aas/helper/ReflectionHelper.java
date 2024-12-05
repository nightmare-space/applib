package com.nightmare.aas.helper;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/// 反射工具类
public class ReflectionHelper {

    public static void print(Object object) {
        System.out.println(">>>>" + object.toString());
        System.out.flush();
    }

    public static <T> T unsafeCast(final Object obj) {
        //noinspection unchecked
        return (T) obj;
    }

    public static <T> T getHiddenField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return unsafeCast(field.get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeHiddenMethod(Object obj, String methodName, Object... args) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Integer) {
                    parameterTypes[i] = int.class;
                } else if (args[i] instanceof Boolean) {
                    parameterTypes[i] = boolean.class;
                } else {
                    parameterTypes[i] = args[i].getClass();
                }
            }
            Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return unsafeCast(method.invoke(obj, args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static HashMap<String, String> map = new HashMap<>();


    public static Object invokeMethod(Object object, String methodName, Object... args) {
        try {
            Class<?>[] classes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Integer) {
                    classes[i] = int.class;
                } else if (args[i] instanceof Boolean) {
                    classes[i] = boolean.class;
                } else {
                    classes[i] = args[i].getClass();
                }
            }
            Method method = object.getClass().getDeclaredMethod(methodName, classes);
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();

        }
        return null;
    }

    public static void listAllObject(Object object) {
        listAllObject(object.getClass());
    }

    public static void listAllObject(Class<?> clazz) {
        map.put("class java.lang.String", "String");
        map.put("java.util.List", "List");
        try {
            print("Class " + clazz.getName());
            // 反射属性字段
            Field[] fields = clazz.getDeclaredFields();

            // 反射方法字段
            Method[] methods = clazz.getDeclaredMethods();

            // 反射构造器
            Constructor<?>[] constuctors = clazz.getDeclaredConstructors();
            print("Constructor========");
            for (Constructor<?> c : constuctors) {
                System.out.print((char) 0x1b);
                System.out.print("[34m");
                System.out.flush();
                print("NAME:" + c.getName());
            }
            System.out.print("\n");
            print("Field========");
            for (Field f : fields) {
                boolean isStatic = Modifier.isStatic(f.getModifiers());
                System.out.print((char) 0x1b + "[36m");
                if (isStatic) {
                    System.out.print("static ");
                }
                System.out.print(f.getType());
                System.out.print((char) 0x1b);
//                System.out.println("[0;32m NAME:" + (char) 0x1b + "[31m" + f.getName() + (char) 0x1b + "[0m");
                System.out.println("[0;32m " + (char) 0x1b + "[32m" + f.getName() + (char) 0x1b + "[0m");
                System.out.flush();
            }
            System.out.print("\n");
            print("Method========");
            for (Method m : methods) {
                boolean isStatic = Modifier.isStatic(m.getModifiers());
                String returnType = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    returnType = m.getReturnType().getTypeName();
                }
                if (map.containsKey(returnType)) {
                    returnType = map.get(returnType);
                }
//                returnType = returnType.replace("class ", "");
                if (isStatic) {
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
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

}
