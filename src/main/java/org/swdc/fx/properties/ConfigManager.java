package org.swdc.fx.properties;
import org.swdc.fx.container.Container;
import org.swdc.fx.anno.Properties;

import java.util.HashMap;

public class ConfigManager extends Container<FXProperties> {

    private HashMap<Class, PropertiesResolver> resolvers = new HashMap<>();

    private String assetsPath = "assets/";

    public <T extends FXProperties> T getOverrideableProperties(Class<T> clazz) {
       return super.getComponentOverrideable(clazz);
    }

    public String getAssetsPath() {
        return assetsPath;
    }

    public void setAssetsPath(String assetsPath) {
        this.assetsPath = assetsPath;
    }

    @Override
    protected <R extends FXProperties> R instance(Class<R> clazz) {
        try {
            Properties properties = clazz.getAnnotation(Properties.class);
            PropertiesResolver resolver = resolvers.get(properties.resolve());
            if (resolver == null) {
                resolver = properties.resolve().getConstructor(ConfigManager.class).newInstance(this);
                resolvers.put(properties.resolve(), resolver);
            }
            FXProperties prop = resolver.load(clazz);
            prop.setResolver(resolver);
            return (R)prop;
        } catch (Exception ex) {
            logger.error("fail to load properties: "+ clazz.getSimpleName());
            return null;
        }
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return FXProperties.class.isAssignableFrom(clazz);
    }
}
