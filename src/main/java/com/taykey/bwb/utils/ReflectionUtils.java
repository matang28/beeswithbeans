package com.taykey.bwb.utils;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Set;

/**
 * Created by matan on 26/09/2017.
 */
public class ReflectionUtils {

    public static <T> Set<Class<? extends T>> allClassesImplementedBy(Class<T> interfaceClass){
        Reflections reflections = new Reflections("com.taykey.bwb", new SubTypesScanner(true));
        return reflections.getSubTypesOf(interfaceClass);
    }

}
