package com.app.framework.util;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.lang.annotation.Annotation;

public class Util {
    public static List<String> findClasses(String packageName, Class<? extends Annotation> annotation) throws Exception {
        List<String> classes = new ArrayList<>();

        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            return classes;
        }

        File directory = new File(resource.toURI());

        for (File file: directory.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(annotation)) {
                    classes.add(className);
                }
            }
        }

        return classes;
    }
}
