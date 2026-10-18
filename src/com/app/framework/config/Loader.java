package com.app.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Loader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Loader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("Fichier 'config.properties' introuvable.");
            }

            properties.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}