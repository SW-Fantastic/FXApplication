package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.event.AppEvent;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.FXProperties;
import org.swdc.fx.services.Service;
import org.swdc.fx.services.ServiceManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * 作为组件的基类使用，提供各种获取组件的方法。
 * 提供一定程度的注入支持。
 */
public class AppComponent implements LifeCircle {

    private ApplicationContainer container;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setContainer(ApplicationContainer container) {
        this.container = container;
    }

    protected final <T extends FXView> T findView(Class<T> view) {
        return container
                .getComponent(ViewManager.class)
                .getComponent(view);
    }

    protected final <T extends FXProperties> T findProperties(Class<T> config) {
        return container
                .getComponent(ConfigManager.class)
                .getOverrideableProperties(config);
    }

    protected final <T extends Service> T findService(Class<T> service) {
        return container
                .getComponent(ServiceManager.class)
                .getComponent(service);
    }

    protected final FXTheme findTheme() {
        ViewManager viewManager = container.getComponent(ViewManager.class);
        return viewManager.getTheme();
    }

    public <T> T findExtraComponent(Class<? extends ExtraModule> extra, Class<T> compClazz) {
        ExtraManager manager = container.getComponent(ExtraManager.class);
        ExtraModule module = manager.getComponent(extra);
        if (module == null) {
            return null;
        } else {
            return (T) module.getComponent(compClazz);
        }
    }

    public <T> T findComponent(Class<T> clazz) {
        List<Container> containers = container.listComponents();
        for (Container container: containers) {
            if (container.isComponentOf(clazz)) {
                return (T)container.getComponent(clazz);
            }
        }

        ExtraManager extraManager = container.getComponent(ExtraManager.class);
        List<ExtraModule> modules = extraManager.listComponents();
        for (ExtraModule module : modules) {
            if (module.isComponentOf(clazz)) {
                return (T)module.getComponent(clazz);
            }
        }
        return null;
    }

    public String getAssetsPath() {
        SFXApplication application = container.getApplication()
                .getClass().getAnnotation(SFXApplication.class);
        return new File(application.assetsPath()).getAbsolutePath();
    }

    public <T> List<T> getScoped(Class<T> scopeClazz) {
        List<Container> containers = container.listComponents();
        for (Container container: containers) {
            if (container.isComponentOf(scopeClazz)) {
                return container.listComponents();
            }
        }
        return Collections.emptyList();
    }

    public String getThemeAssetsPath() {
        return new File(new StringBuilder(getAssetsPath())
                .append(File.separator)
                .append("theme")
                .append(File.separator)
                .append(findTheme() == null ? "default" : findTheme().getName()).toString())
                .getAbsolutePath();
    }

    public void emit(AppEvent event) {
        this.container.emit(event);
    }

}
