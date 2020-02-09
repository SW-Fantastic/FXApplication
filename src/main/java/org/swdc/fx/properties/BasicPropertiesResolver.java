package org.swdc.fx.properties;

import org.swdc.fx.anno.Properties;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;

public class BasicPropertiesResolver implements PropertiesResolver {

    private ConfigManager configManager;

    public BasicPropertiesResolver(ConfigManager environment) {
        this.configManager = environment;
    }

    @Override
    public void saveProperties(FXProperties props) {

    }

    @Override
    public FXProperties load(Class clazz) {
        Properties properties = (Properties) clazz.getAnnotation(Properties.class);
        if (properties == null) {
            return null;
        }
        if (properties.value().equals("")) {
            return null;
        }
        if (properties.resolve() != this.getClass()) {
            return null;
        }
        java.util.Properties prop = new java.util.Properties();
        try(FileInputStream propSource = new FileInputStream(configManager.getAssetsPath() + File.separator + properties.value())) {
            prop.load(propSource);
            FXProperties instance = (FXProperties) clazz.getConstructor().newInstance();
            Class targetClazz = clazz;
            while (targetClazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    try {
                        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),targetClazz);
                        propertyDescriptor.getWriteMethod().invoke(instance, prop.getProperty(properties.prefix() + "." + field.getName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (targetClazz.getSuperclass() != null) {
                    targetClazz = targetClazz.getSuperclass();
                    if (targetClazz == Object.class) {
                        break;
                    }
                    if (targetClazz == FXProperties.class) {
                        break;
                    }
                }
            }
            return instance;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public FXProperties refresh(FXProperties target) {
        return null;
    }
}
