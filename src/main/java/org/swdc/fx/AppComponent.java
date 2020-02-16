package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.FXProperties;
import org.swdc.fx.services.Service;
import org.swdc.fx.services.ServiceManager;

import java.lang.reflect.Field;
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

    public static void awareComponents(AppComponent component) throws IllegalAccessException {
        Class clazz = component.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Aware aware = field.getAnnotation(Aware.class);
                if (aware == null) {
                    continue;
                }
                Object target = component.findComponent(field.getType());
                field.setAccessible(true);
                field.set(component,target);
                field.setAccessible(false);
            }
            clazz = clazz.getSuperclass();
            if (clazz == AppComponent.class || clazz == Object.class) {
                break;
            }
        }
    }

}
