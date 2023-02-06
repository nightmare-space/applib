package com.nightmare.applib_util.utils;

public class Lg {
    public static void d(Object object) {
        System.out.print((char) 0x1b + "[38;5;42m·");
        System.out.print((char) 0x1b + "[0m ");
        System.out.print((char) 0x1b + "[38;5;38m");
        System.out.print(object.toString());
        System.out.println((char) 0x1b + "[0m");
        System.out.flush();
    }
}
