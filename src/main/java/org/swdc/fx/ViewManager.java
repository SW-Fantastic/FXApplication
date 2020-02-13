package org.swdc.fx;

import javafx.fxml.FXMLLoader;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理View的容器。
 */
public class ViewManager extends Container<FXView> {

    private Map<Class, FXView> views = new HashMap<>();

    private FXTheme theme = null;

    /**
     * 获取一个view，没有的话就创建一个。
     * @param clazz view的class
     * @param <R> View的子类类型
     * @return View的实例
     */
    @Override
    public <R extends FXView> R getComponent(Class<R> clazz) {
        if (theme == null) {
            ConfigManager configManager = getScope().getComponent(ConfigManager.class);
            DefaultUIConfigProp prop = configManager.getOverrideableProperties(DefaultUIConfigProp.class);
            theme = new FXTheme(prop.getTheme(), configManager.getAssetsPath());
        }
        if (views.containsKey(clazz)) {
            return (R)views.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

    /**
     * 在容器中注册一个view，如果有的话就直接返回，没有就创建一个新的。
     * 会检查Scope注解，如果没有或者single，则会使用容器存储这个view
     * 否则直接返回实例，不缓存。
     *
     * @param clazz view的类型
     * @param <R> view子类的泛型
     * @return view的实例
     */
    @Override
    public <R extends FXView> FXView register(Class<R> clazz) {
        Scope scope = clazz.getAnnotation(Scope.class);

        if (views.containsKey(clazz)) {
            logger.error(" view is existed.");
            return views.get(clazz);
        } else {
            try {
                Constructor constructorNoArgs = clazz.getConstructor();
                if (constructorNoArgs == null) {
                    throw new RuntimeException(" No Args constructor should be provided");
                }
                FXView view = (FXView) constructorNoArgs.newInstance();
                view.setContainer((ApplicationContainer) this.getScope());
                if(!view.loadFxmlView()) {
                    view.createView();
                } else {
                    FXMLLoader loader = view.getLoader();
                    if (loader != null) {
                        Object controller = loader.getController();
                        if (controller != null && controller instanceof FXController) {
                            FXController fxController = (FXController) controller;
                            fxController.setContainer((ApplicationContainer) this.getScope());
                        }
                    }
                }

                this.activeExtras(view);

                view.initialize();
                theme.initView(view);
                if (scope == null || scope.value() == ScopeType.SINGLE) {
                    views.put(clazz,view);
                    logger.info(" view loaded :" + view.getClass().getSimpleName());
                }
                return view;
            } catch (Exception ex) {
                logger.error(" error when loading view :", ex);
                return null;
            }
        }
    }

    /**
     * 列出所有view
     * @return
     */
    @Override
    public List<FXView> listComponents() {
        return new ArrayList<>(views.values());
    }

}
