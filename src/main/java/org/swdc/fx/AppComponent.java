package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.FXProperties;
import org.swdc.fx.services.Service;
import org.swdc.fx.services.ServiceManager;

/**
 * 作为组件的基类使用，提供各种获取组件的方法。
 */
public class AppComponent implements LifeCircle {

    private ApplicationContainer container;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setContainer(ApplicationContainer container) {
        this.container = container;
    }

    public <T extends FXView> T findView(Class<T> view) {
        return container
                .getComponent(ViewManager.class)
                .getComponent(view);
    }

    public <T extends FXProperties> T findProperties(Class<T> config) {
        return container
                .getComponent(ConfigManager.class)
                .getOverrideableProperties(config);
    }

    public <T extends Service> T findService(Class<T> service) {
        return container
                .getComponent(ServiceManager.class)
                .getComponent(service);
    }

}
