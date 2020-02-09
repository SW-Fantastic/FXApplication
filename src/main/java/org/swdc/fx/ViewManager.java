package org.swdc.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ViewManager extends Container<FXView> {

    private Map<Class, FXView> views = new HashMap<>();

    private FXTheme theme = null;

    @Override
    public <R extends FXView> R getComponent(Class<R> clazz) {
        if (theme == null) {
            ConfigManager configManager = getScope().getComponent(ConfigManager.class);
            DefaultUIConfigProp prop = configManager.getOverrideableProperties(DefaultUIConfigProp.class);
            theme = new FXTheme(prop.getTheme());
        }
        if (views.containsKey(clazz)) {
            return (R)views.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

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
                    Parent parent = view.createView();
                    if (parent == null) {
                        logger.error(" can not create create view: " + clazz.getSimpleName());
                        return null;
                    }
                    view.setParent(parent);
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
                view.onInitialized();
                theme.initView(view);
                if (scope == null || scope.value() == ScopeType.SINGLE) {
                    views.put(clazz,view);
                    logger.info(" view loaded :" + view.getClass());
                }
                return view;
            } catch (Exception ex) {
                logger.error(" error when loading view :", ex);
                return null;
            }
        }
    }
}
