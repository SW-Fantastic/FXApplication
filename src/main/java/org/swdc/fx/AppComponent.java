package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.FXTheme;
import org.swdc.fx.FXView;
import org.swdc.fx.LifeCircle;
import org.swdc.fx.ViewManager;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.container.Container;
import org.swdc.fx.event.AppEvent;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.FXProperties;
import org.swdc.fx.properties.Language;
import org.swdc.fx.properties.Languages;
import org.swdc.fx.services.Service;
import org.swdc.fx.services.ServiceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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

    /**
     * 最重要的是Scope为Cache的时候的组件，
     * 关于这种组件只能使用此方法获取。
     * @param clazz 目标组件的类
     * @param condition 条件
     * @param <T> 类泛型
     * @return 组件对象
     */
    public <T> T findExistedComponent(Class<T> clazz, Predicate<T> condition) {
        List<Container> containers = container.listComponents();
        Container container = null;
        for (Container containerItem: containers) {
            if (containerItem.isComponentOf(clazz)) {
                container = containerItem;
                break;
            }
        }
        List<Object> target = container.listComponents();
        for (Object item : target) {
            try {
                if (condition.test((T)item)) {
                    return (T)item;
                }
            } catch (ClassCastException ignored) {
            }
        }
        return null;
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

    protected <T> T getExtraModule(Class<? extends ExtraModule> extra) {
        ExtraManager manager = container.getComponent(ExtraManager.class);
        ExtraModule module = manager.getComponent(extra);
        if (module == null) {
            return null;
        } else {
            return (T) module;
        }
    }

    /**
     * 查找组件，除了single的组件之外，都是新建的组件。
     * single组件会直接返回已有的单例实例。
     * @param clazz 组件类
     * @param <T> 组件泛型
     * @return
     */
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
        List<T> result = new ArrayList<>();
        for (Container container: containers) {
            if (container.isComponentOf(scopeClazz)) {
               List<Object> content = container.listComponents();
               for (Object comp: content){
                   if (scopeClazz.isAssignableFrom(comp.getClass())) {
                       result.add((T)comp);
                   }
               }
            }
        }
        return result;
    }

    public String getThemeAssetsPath() {
        return new File(new StringBuilder(getAssetsPath())
                .append(File.separator)
                .append("theme")
                .append(File.separator)
                .append(findTheme() == null ? "default" : findTheme().getName()).toString())
                .getAbsolutePath();
    }

    public String i18n(String text) {
        if (text.startsWith(Languages.LANGUAGE_PREFIX)) {
            text = text.replace(Languages.LANGUAGE_PREFIX,"");
            Languages languages = findComponent(Languages.class);
            return languages.getLanguage(text);
        }
        return text;
    }

    public void emit(AppEvent event) {
        this.container.emit(event);
    }

}
