package com.chetuan.askapp.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Bean2Map {
	
    public static Map<String, String> toMap(Object javaBean) 
    { 
        Map<String, String> result = new HashMap<String, String>(); 
        Method[] methods = javaBean.getClass().getDeclaredMethods(); 

        for (Method method : methods) 
        { 
            try 
            { 
                if (method.getName().startsWith("get")) 
                { 
                    String field = method.getName(); 
                    field = field.substring(field.indexOf("get") + 3); 
                    field = field.toLowerCase().charAt(0) + field.substring(1); 

                    Object value = method.invoke(javaBean, (Object[])null); 
                    result.put(field, null == value ? "" : value.toString()); 
                } 
            } 
            catch (Exception e) 
            { 
            } 
        } 

        return result; 
    } 
}
