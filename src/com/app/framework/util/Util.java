package com.app.framework.util;

import com.app.framework.model.UrlMethod;
import com.app.framework.model.UrlMethodMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mg.itu.framework.annotation.UrlMapping;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.PrintWriter;
import java.io.StringWriter;
import jakarta.servlet.http.Part;
import com.app.framework.model.MapFile;
import java.util.Collection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.app.framework.config.Loader;
import com.app.framework.model.SSession;
import jakarta.servlet.http.HttpSession;
import java.util.Enumeration;
import jakarta.servlet.ServletContext;

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

    public static void findUrlMethodMappings(List<String> packageNames, Map<UrlMethod, UrlMethodMapping> urlMethodMappings, Class<? extends Annotation> classAnnotation, Class<? extends Annotation> methodAnnotation) throws Exception {
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
                        
                        System.out.println(clazz.getName());

                        for (Method m: methods) {

                            System.out.println("  " + m.getName());

                            if (m.isAnnotationPresent(methodAnnotation)) {
                                Annotation methAnnotation = m.getAnnotation(methodAnnotation);

                                if (methAnnotation.annotationType() == UrlMapping.class) {
                                    UrlMapping urlMapping = (UrlMapping) methAnnotation;
                                    String url = urlMapping.path();
                                    String httpMethod = urlMapping.method();

                                    UrlMethod urlMethod = new UrlMethod(url, httpMethod);
                                    
                                    UrlMethodMapping mapping = new UrlMethodMapping(clazz, m);

                                    if (!urlMethodMappings.containsKey(urlMethod)) {
                                        urlMethodMappings.put(urlMethod, mapping);
                                    } else {
                                        throw new Exception("Duplicate URL mapping found for " + httpMethod + " " + url + " in class " + clazz.getName() + " method " + m.getName());
                                    }
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

    public static String toJson(ObjectMapper mapper, Object o) throws Exception {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

    public static Object convert(String value, Class<?> type) {
        Object conversion = null;

        if (value == null) {
            return null;
        }

        if (type.equals(int.class) || type.equals(Integer.class)) {
            conversion = Integer.parseInt(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            conversion = Long.parseLong(value);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            conversion = Float.parseFloat(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            conversion = Double.parseDouble(value);
        } else if (type.equals(String.class)) {
            conversion = value;
        } else if (type.equals(char.class) || type.equals(Character.class)) {
            conversion = value.charAt(0);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            conversion = Boolean.parseBoolean(value);
        } else if (type.equals(LocalDate.class)) {
            conversion = LocalDate.parse(value);
        } else if (type.equals(LocalDateTime.class)) {
            conversion = LocalDateTime.parse(value);
        }

        return conversion;
    }

    public static Object convert(String[] values, Class<?> type) {
        if (values == null || values.length == 0) {
            return null;
        }

        Object conversion = Array.newInstance(type, values.length);

        for (int i = 0; i < values.length; i++) {
            Object value = convert(values[i], type);
            Array.set(conversion, i, value);
        }

        return conversion;
    }

    public static boolean isRequestClass(Class<?> clazz, List<String> requestPackages) {
        for (String requestPackage: requestPackages) {
            if (clazz.getName().startsWith(requestPackage + ".")) {
                return true;
            }
        }

        return false;
    }

    public static Object bind(Class<?> clazz, Map<String, String[]> params, int fieldIndex, List<String> requestPackages) throws Exception {
        Constructor<?> c = clazz.getDeclaredConstructor();
        Object instance = c.newInstance();

        Field[] fields = instance.getClass().getDeclaredFields();

        for (Field field: fields) {
            field.setAccessible(true);

            String name = field.getName();
            Class<?> type = field.getType();

            Object conversion = null;

            if (type.isArray()) {
                Class<?> elementType = type.getComponentType();

                if (!Util.isRequestClass(elementType, requestPackages)) {
                    String[] values = params.get(name) != null ? params.get(name) : null;
                    conversion = Util.convert(values, elementType);
                } else {
                    conversion = Util.bind(elementType, params, requestPackages);
                }
            } else if (Util.isRequestClass(type, requestPackages)) {
                conversion = bind(type, params, fieldIndex, requestPackages);
            } else {
                String value = params.get(name) != null ? params.get(name)[fieldIndex] : null;
                conversion = Util.convert(value, type);
            }

            Util.set(instance, field, conversion);
        }

        return instance;
    }

    public static int findLength(Class<?> clazz, Map<String, String[]> params, List<String> requestPackages) {
        Field[] fields = clazz.getDeclaredFields();

        int length = 0;

        for (Field field: fields) {
            field.setAccessible(true);

            String name = field.getName();
            Class<?> type = field.getType();

            if (Util.isRequestClass(type, requestPackages)) {
                return findLength(type, params, requestPackages);
            }

            String[] values = params.get(name) != null ? params.get(name) : null;
            length = values != null && values.length > 0 ? values.length : length;
        }

        return length;
    }

    public static Object bind(Class<?> clazz, Map<String, String[]> params, List<String> requestPackages) throws Exception {
        int length = Util.findLength(clazz, params, requestPackages);

        Object list = Array.newInstance(clazz, length);

        for (int i = 0; i < length; i++) {
            Object instance = bind(clazz, params, i, requestPackages);
            Array.set(list, i, instance);
        }

        return list;
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void set(Object instance, Field field, Object value) throws Exception {
        Class<?>[] paramTypes = { field.getType() };
        Object[] args = { value };

        Method method = instance.getClass().getDeclaredMethod("set" + Util.capitalize(field.getName()), paramTypes);
        method.invoke(instance, args);
    }

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        return sw.toString();
    }

    public static MapFile toMapFile(Part file) throws Exception {
        if (file == null || file.getSize() == 0) {
            throw new Exception("Fichier introuvable ou contenu vide.");
        }

        String nomComplet = file.getSubmittedFileName();

        int index = nomComplet.lastIndexOf('.');

        String nom = UUID.randomUUID().toString();
        String extension;

        if (index > 0) {
            extension = nomComplet.substring(index + 1);
        } else {
            extension = "";
        }

        byte[] content = file.getInputStream().readAllBytes();

        return new MapFile(nom, extension, content);
    }

    public static MapFile[] toMapFiles(Collection<Part> files, String fieldName) throws Exception {
        List<MapFile> mapFilesList = new ArrayList<>();

        for (Part file: files) {
            if (file.getName().equals(fieldName)) {
                mapFilesList.add(Util.toMapFile(file));
            }
        }

        return mapFilesList.toArray(MapFile[]::new);
    }

    public static void saveUploadedFile(MapFile file) throws Exception {
        String fileName = file.getNom() + "." + file.getExtension();
        String uploadDir = Loader.get("upload.directory");

        Path folderPath = Paths.get(uploadDir);

        Files.createDirectories(folderPath);
        Path filePath = folderPath.resolve(fileName);

        Files.write(filePath, file.getContent());
    }

    public static void saveUploadedFiles(MapFile[] files) throws Exception {
        for (MapFile file: files) {
            Util.saveUploadedFile(file);
        }
    }

    public static SSession getSSession(ServletContext context, HttpSession httpSession) {
        SSession session = null;

        if (context.getAttribute(httpSession.getId()) == null) {
            session = new SSession();
        } else {
            session = (SSession) context.getAttribute(httpSession.getId());
        }

        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = httpSession.getAttribute(attributeName);
            session.setAttribute(attributeName, attributeValue);
        }

        context.setAttribute(httpSession.getId(), session);
        return session; 
    }

    public static void updateHttpSession(HttpSession httpSession, SSession sSession) {
        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            httpSession.removeAttribute(attributeName);
        }

        for (Map.Entry<String, Object> entry : sSession.getAttributes().entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
