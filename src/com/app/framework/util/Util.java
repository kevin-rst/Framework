package com.app.framework.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.app.framework.model.UrlMethodMapping;
import java.lang.reflect.Method;
import mg.itu.framework.annotation.UrlMapping;

public class Util {
    public static List<String> findClasses(List<String> packageNames, Class<? extends Annotation> classAnnotation) throws Exception {
        List<String> classes = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String packageName: packageNames) {
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                continue;
            }
    
            File directory = new File(resource.toURI());
    
            for (File file: directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
    
                    if (clazz.isAnnotationPresent(classAnnotation)) {
                        classes.add(className);
                    }
                }
            }
        }

        return classes;
    }

    public static void findUrlMethodMappings(List<String> packageNames, Map<String, UrlMethodMapping> urlMethodMappings, Class<? extends Annotation> classAnnotation, Class<? extends Annotation> methodAnnotation) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String packageName: packageNames) {
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                continue;
            }
    
            File directory = new File(resource.toURI());
    
            for (File file: directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
    
                    if (clazz.isAnnotationPresent(classAnnotation)) {
                        Method[] methods = clazz.getDeclaredMethods();

                        for (Method m: methods) {
                            if (m.isAnnotationPresent(methodAnnotation)) {
                                Annotation methAnnotation = m.getAnnotation(methodAnnotation);

                                if (methAnnotation.annotationType() == UrlMapping.class) {
                                    UrlMapping urlMapping = (UrlMapping) methAnnotation;
                                    String url = urlMapping.value();
                                    
                                    UrlMethodMapping mapping = new UrlMethodMapping(clazz, m);
                                    urlMethodMappings.put(url, mapping);
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public static List<String> splitString(String str, String separator) {
        String[] splitted = str.split(separator);
        return new ArrayList<>(List.of(splitted));
    }

    public static UrlMethodMapping getUrlSupported(Map<String, UrlMethodMapping> urlMethodMappings, String url) {
        return urlMethodMappings.get(url);
    }
}
