package org.mirza.util;

import lombok.Getter;
import org.mirza.exception.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {
    @Getter
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = PropertyUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new ConfigurationException("application.properties not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

}
