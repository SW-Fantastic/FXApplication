package org.swdc.fx.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.Properties;

import java.util.HashMap;

public class ConfigManager {

    private HashMap<Class, Object> configurations = new HashMap<>();

    private HashMap<Class, PropertiesResolver> resolvers = new HashMap<>();

    private String assetsPath = "assets/";

    private Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    public <T extends FXProperties> void addProperties(Class<T> clazz) {
        if (configurations.containsKey(clazz)) {
            throw new RuntimeException("配置已经存在 ：" + clazz);
        } else {
            try {
                Properties properties = clazz.getAnnotation(Properties.class);
                PropertiesResolver resolver = resolvers.get(properties.resolve());
                if (resolver == null) {
                    resolver = properties.resolve().getConstructor(ConfigManager.class).newInstance(this);
                    resolvers.put(properties.resolve(),resolver);
                }
                Object prop = resolver.load(clazz);
                logger.info("properties loaded: " + prop.getClass());
                configurations.put(clazz, prop);
            } catch (Exception ex) {
                logger.error("properties load failed: ", ex);
            }
        }
    }

    public <T extends FXProperties> T getProperties(Class<T> clazz) {
        if (configurations.containsKey(clazz)) {
            return (T)configurations.get(clazz);
        } else {
            try {
                Properties properties = clazz.getAnnotation(Properties.class);
                PropertiesResolver resolver = resolvers.get(properties.resolve());
                if (resolver == null) {
                    resolver = properties.resolve()
                            .getConstructor(ConfigManager.class)
                            .newInstance(this);
                    resolvers.put(properties.resolve(),resolver);
                }
                Object prop = resolver.load(clazz);
                configurations.put(clazz, prop);
                return (T) prop;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    public <T extends FXProperties> T getOverrideableProperties(Class<T> clazz) {
        if (configurations.containsKey(clazz)) {
            return (T)configurations.get(clazz);
        }
        if (propertiesExist(clazz, true)) {
            for (Class item : configurations.keySet()) {
                if (clazz.isAssignableFrom(item)){
                    return (T)configurations.get(item);
                }
            }
        }
        logger.warn("properties not found, loading :" + clazz.getSimpleName());
        return getProperties(clazz);
    }

    public boolean propertiesExist(Class<? extends FXProperties> properties, boolean checkAssignable) {
        for (Class clazz : configurations.keySet()) {
            if (clazz == properties) {
                return true;
            }
            if (properties.isAssignableFrom(clazz) && checkAssignable){
                return true;
            }
        }
        return false;
    }

    public String getAssetsPath() {
        return assetsPath;
    }

    public void setAssetsPath(String assetsPath) {
        this.assetsPath = assetsPath;
    }
}
