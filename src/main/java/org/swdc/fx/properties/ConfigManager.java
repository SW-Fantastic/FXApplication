package org.swdc.fx.properties;

import org.swdc.fx.Container;
import org.swdc.fx.anno.Properties;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;

import java.util.HashMap;

public class ConfigManager extends Container<FXProperties> {

    private HashMap<Class, Object> configurations = new HashMap<>();

    private HashMap<Class, PropertiesResolver> resolvers = new HashMap<>();

    private String assetsPath = "assets/";

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
        return (T)register(clazz);
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

    @Override
    public <R extends FXProperties> R getComponent(Class<R> clazz) {
        if (configurations.containsKey(clazz)) {
            return (R)configurations.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

    @Override
    public <R extends FXProperties> FXProperties register(Class<R> clazz) {
        if (configurations.containsKey(clazz)) {
            throw new RuntimeException("配置已经存在 ：" + clazz);
        } else {
            try {
                Scope scope = clazz.getAnnotation(Scope.class);
                Properties properties = clazz.getAnnotation(Properties.class);
                PropertiesResolver resolver = resolvers.get(properties.resolve());
                if (resolver == null) {
                    resolver = properties.resolve().getConstructor(ConfigManager.class).newInstance(this);
                    resolvers.put(properties.resolve(),resolver);
                }
                Object prop = resolver.load(clazz);
                if (scope == null || scope.value() == ScopeType.SINGLE) {
                    configurations.put(clazz, prop);
                    logger.info("properties loaded: " + prop.getClass());
                }
                return (R)prop;
            } catch (Exception ex) {
                logger.error("properties load failed: ", ex);
                return null;
            }
        }
    }
}
