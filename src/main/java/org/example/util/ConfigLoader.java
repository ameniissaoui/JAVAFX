package org.example.util;

import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(ConfigLoader.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            System.err.println("Failed to load application.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}