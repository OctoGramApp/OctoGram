package it.octogram.android.utils;

import java.util.HashMap;
import java.util.Map;

public class JavaUtils {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();
    static {
        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPERS.put(char.class, Character.class);
        PRIMITIVE_WRAPPERS.put(short.class, Short.class);
        PRIMITIVE_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVE_WRAPPERS.put(long.class, Long.class);
        PRIMITIVE_WRAPPERS.put(float.class, Float.class);
        PRIMITIVE_WRAPPERS.put(double.class, Double.class);
    }

    public static boolean isInstanceOf(Class<?> c1, Class<?> c2) {
        if (c1.isPrimitive() || c2.isPrimitive()) {
            if (c1.isPrimitive() && c2.isPrimitive()) {
                return c1 == c2;
            }
            Class<?> wrapperClass = c1.isPrimitive() ? PRIMITIVE_WRAPPERS.get(c1) : PRIMITIVE_WRAPPERS.get(c2);
            if (wrapperClass == null) {
                return false;
            }
            if (c1.isPrimitive()) {
                c1 = wrapperClass;
            } else {
                c2 = wrapperClass;
            }
        }
        return c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1);
    }
}
