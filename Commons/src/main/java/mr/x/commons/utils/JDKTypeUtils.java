/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mr.x.commons.utils;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author zhangwei
 */
public class JDKTypeUtils {
    
    
    static HashSet<Class<? extends Object>> wrapperType = new HashSet<>(Arrays.asList(
    Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));
    
    public static boolean isPrimitive(Object o) {
        return o.getClass().isPrimitive();
    }
    
    public static boolean isWrapperType(Object o) {
        return wrapperType.contains(o.getClass());
    }
    
    public static boolean isPrimitiveOrWrapper(Object o) {
        return isPrimitive(o) || isWrapperType(o);
    }
}
