package com.blackbox.lerist.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Lerist on 2017/06/14 0014.
 * Class调用工具
 */

public class ClassInvoker {
    public static <E extends Object> E invoke(Object receiver, String className, String methodName, Class[] parameterTypes, Object[] args) throws Exception {
        return (E) invoke(receiver, Class.forName(className), methodName, parameterTypes, args);
    }

    public static <E extends Object> E invoke(Object receiver, Class c, String methodName, Class[] parameterTypes, Object[] args) throws Exception {
        try {
            Method declaredMethod = c.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return (E) declaredMethod.invoke(receiver, args);
        } catch (Exception e) {
            throw e;
        }
    }

    public static <E extends Object> E getDeclaredField(Object receiver, Class c, String fieldName) {
        try {
            Field declaredField = c.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return (E) declaredField.get(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
