package org.swdc.fx.properties;

import org.swdc.fx.anno.Properties;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),targetClazz);
                    Method writer = propertyDescriptor.getWriteMethod();
                    Parameter param = writer.getParameters()[0];
                    try {
                        if (param.getType() == String.class) {
                            propertyDescriptor.getWriteMethod().invoke(instance, prop.getProperty(properties.prefix() + "." + field.getName()));
                            continue;
                        }
                        Method covertStatic = param.getType().getMethod("valueOf", String.class);
                        if (covertStatic != null) {
                            Object paramObj = covertStatic.invoke(null, prop.getProperty(properties.prefix() + "." + field.getName()));
                            propertyDescriptor.getWriteMethod().invoke(instance, paramObj);
                        }
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
