package me.jackson.utlis;

/**
 * @author Jackson Chen
 * @version 1.0
 * @date 2023/2/22
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Used To Reading properties in config file.
 */
public class ConfigReader {
    private static InputStream input = null;
    public static Properties prop = new Properties();

    /**
     * Reading the .properties and load into properties
     */
    static {
        try {

            input = new FileInputStream("./src/main/resources/.properties");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String read(String key) {
        return prop.getProperty(key);
    }

}
