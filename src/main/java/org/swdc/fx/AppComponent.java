package org.swdc.fx;

import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.FXProperties;
import org.swdc.fx.services.Service;
import org.swdc.fx.services.ServiceManager;

public class AppComponent {

    private ApplicationContainer container;

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
