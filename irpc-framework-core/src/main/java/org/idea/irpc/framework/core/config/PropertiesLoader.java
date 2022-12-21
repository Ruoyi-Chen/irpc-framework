package org.idea.irpc.framework.core.config;

import org.idea.irpc.framework.core.common.utils.CommonUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/17 15:21
 */
public class PropertiesLoader {
    private static Properties properties;

    private static String DEFAULT_PROPERTIES_FILE = "E:\\Java_projects\\irpc-framework\\irpc-framework-core\\src\\main\\resources\\irpc.properties";

    private static Map<String, String> propertiesMap = new HashMap<>();

    // TODO 如果这里直接使用static修饰是否可以？
    public static void loadConfiguration() throws IOException {
        if (properties != null) {
            return;
        }

        properties = new Properties();
        FileInputStream in = null;
        in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
        properties.load(in);
    }
    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static String getPropertiesStr(String key) {
        if (properties == null) {
            return null;
        }
        if (CommonUtils.isEmpty(key)) {
            return null;
        }
        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return String.valueOf(propertiesMap.get(key));
    }


    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesInteger(String key) {
        if (properties == null) {
            return null;
        }

        if (CommonUtils.isEmpty(key)){
            return null;
        }

        if (!propertiesMap.containsKey(key)) {
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }

    public static String getPropertiesStrDefault(String key, String defaultVal) {
        String val = getPropertiesStr(key);
        return val == null || val.equals("") ? defaultVal : val;
    }

    public static String getPropertiesNotBlank(String key) {
        String val = getPropertiesStr(key);
        if (val == null || val.equals("")) {
            throw new IllegalArgumentException(key + " 配置为空异常");
        }
        return val;
    }
}
