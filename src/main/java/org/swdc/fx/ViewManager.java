package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ViewManager {

    private Map<Class, FXView> views = new HashMap<>();

    private ConfigManager configManager;

    private FXTheme theme = null;

    private Logger logger = LoggerFactory.getLogger(ViewManager.class);

    public ViewManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public <T extends FXView> T getView(Class<T> clazz) {
        if (theme == null) {
            DefaultUIConfigProp prop = configManager.getOverrideableProperties(DefaultUIConfigProp.class);
            theme = new FXTheme(prop.getTheme());
        }
        if (views.containsKey(clazz)) {
            return (T)views.get(clazz);
        } else {
            try {
                Constructor constructorNoArgs = clazz.getConstructor();
                if (constructorNoArgs == null) {
                    throw new RuntimeException("No Args constructor should be provided");
                }
                FXView view = (FXView) constructorNoArgs.newInstance();
                view.setManagers(this, this.configManager);
                theme.initView(view);
                views.put(clazz,view);
                logger.info("view loaded :" + view.getClass());
                return (T)view;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

}
