package org.openjava.upay.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

public class ObjectUtils
{
    public static boolean equals(String str1, String str2)
    {
        return StringUtils.equals(str1, str2);
    }

    public static String[] split(String str, char separator)
    {
        if (str == null) {
            return new String[0];
        }

        return StringUtils.split(str, separator);
    }

    public static boolean isEmpty(String str)
    {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }

    public static <T> boolean isEmpty(List<T> array)
    {
        return array == null || array.isEmpty();
    }

    public static <T> boolean isNotEmpty(List<T> array)
    {
        return array != null && !array.isEmpty();
    }

    public static String trimToEmpty(String str)
    {
        return StringUtils.trimToEmpty(str);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List getObjList(Enum obj)
    {
        if (obj == null) {
            return new ArrayList();
        }
        return EnumUtils.getEnumList(obj.getClass());
    }

    public static boolean isNull(Object obj)
    {
        return null == obj;
    }

    public static Map<String, Object> transBean2Map(Object obj)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            @SuppressWarnings("rawtypes") Class class1 = obj.getClass();
            if (obj != null) {
                for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
                    String fieldName = field.getName();
                    @SuppressWarnings("unchecked") Method method = class1.getMethod("get" + (fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)));
                    map.put(field.getName(), method.invoke(obj));
                }
                if (obj.getClass().getSuperclass() != null) {
                    for (java.lang.reflect.Field field : obj.getClass().getSuperclass().getDeclaredFields()) {
                        String fieldName = field.getName();
                        @SuppressWarnings("unchecked") Method method = class1.getMethod("get" + (fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)));
                        map.put(field.getName(), method.invoke(obj));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
