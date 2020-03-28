package org.swdc.fx.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.ConfigProp;
import org.swdc.fx.anno.Properties;
import org.swdc.fx.event.ConfigRefreshEvent;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;

public class BasicPropertiesResolver implements PropertiesResolver {

    private ConfigManager configManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public BasicPropertiesResolver(ConfigManager environment) {
        this.configManager = environment;
    }

    @Override
    public void saveProperties(FXProperties props) {
        Properties properties = props.getClass().getAnnotation(Properties.class);
        if (properties == null) {
            return;
        }
        if (properties.value().equals("")) {
            return;
        }
        if (properties.resolve() != this.getClass()) {
            return;
        }
        java.util.Properties targetProp = new java.util.Properties();
        try {
            String path = configManager.getAssetsPath() + File.separator + properties.value();
            FileInputStream inputStream = new FileInputStream(path);
            Class clazz = props.getClass();
            targetProp.load(inputStream);
            inputStream.close();
            for (Field field : clazz.getDeclaredFields()) {
                 PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),clazz);
                 Method reader = propertyDescriptor.getReadMethod();
                 String propData = reader.invoke(props).toString();
                 ConfigProp prop = field.getAnnotation(ConfigProp.class);
                 String name = prop.propName();
                 targetProp.setProperty(properties.prefix() + "." + name, propData);
            }
            FileOutputStream outputStream = new FileOutputStream(path);
            targetProp.store(outputStream,new Date().toString());
            this.configManager.emit(new ConfigRefreshEvent(props, null));
        } catch (Exception e) {
            logger.error("fail to store config", e);
        }
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
            return refresh(instance);
        } catch (Exception ex) {
            logger.error("fail to load config", ex);
        }
        return null;
    }

    @Override
    public FXProperties refresh(FXProperties instance) {
        java.util.Properties prop = new java.util.Properties();
        Class clazz = instance.getClass();
        Properties properties = (Properties) clazz.getAnnotation(Properties.class);
        try(FileInputStream propSource = new FileInputStream(configManager.getAssetsPath() + File.separator + properties.value())) {
            prop.load(propSource);
            Class targetClazz = clazz;
            while (targetClazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),targetClazz);
                    Method writer = propertyDescriptor.getWriteMethod();
                    Parameter param = writer.getParameters()[0];
                    ConfigProp propDesc = field.getAnnotation(ConfigProp.class);
                    try {
                        if (param.getType() == String.class) {
                            if (propDesc != null) {
                                propertyDescriptor.getWriteMethod().invoke(instance, prop.getProperty(properties.prefix() + "." + propDesc.propName()));
                            } else {
                                propertyDescriptor.getWriteMethod().invoke(instance, prop.getProperty(properties.prefix() + "." + field.getName()));
                            }
                            continue;
                        }
                        Method covertStatic = param.getType().getMethod("valueOf", String.class);
                        if (covertStatic != null) {
                            Object paramObj = null;
                            if (propDesc != null) {
                                paramObj = covertStatic.invoke(null, prop.getProperty(properties.prefix() + "." + propDesc.propName()));
                            } else {
                                paramObj = covertStatic.invoke(null, prop.getProperty(properties.prefix() + "." + field.getName()));
                            }
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
            configManager.emit(new ConfigRefreshEvent(instance, null));
            return instance;
        } catch (Exception ex) {
            logger.error("fail to load config", ex);
        }
        return instance;
    }
}
