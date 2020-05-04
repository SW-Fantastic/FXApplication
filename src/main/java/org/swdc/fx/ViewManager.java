package org.swdc.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.container.Container;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理View的容器。
 */
public class ViewManager extends Container<FXView> {

    private FXTheme theme = null;

    private List<Image> icons = new ArrayList<>();

    @Override
    public void initialize() {
        FXApplication application = ((ApplicationContainer) getParent()).getApplication();
        icons = application.loadIcons();
        ConfigManager configManager = getParent().getComponent(ConfigManager.class);
        DefaultUIConfigProp prop = configManager.getOverrideableProperties(DefaultUIConfigProp.class);
        theme = new FXTheme(prop.getTheme(), configManager.getAssetsPath());
    }

    @Override
    protected FXView instance(Class target) {
        try {
            Constructor constructorNoArgs = target.getConstructor();
            if (constructorNoArgs == null) {
                throw new RuntimeException(" No Args constructor should be provided");
            }
            FXView view = (FXView) constructorNoArgs.newInstance();
            view.setContainer((ApplicationContainer) this.getParent());
            if (!view.loadFxmlView()) {
                view.createView();
            } else {
                FXMLLoader loader = view.getLoader();
                if (loader != null) {
                    Object controller = loader.getController();
                    if (controller != null && controller instanceof FXController) {
                        FXController fxController = (FXController) controller;
                        Class ctrlClazz = fxController.getClass();
                        fxController.setContainer((ApplicationContainer) this.getParent());
                        if (ctrlClazz.getModule().isOpen(target.getPackageName(), FXApplication.class.getModule())) {
                            this.awareComponents(fxController);
                        }
                        fxController.setView(view);
                        fxController.initialize();
                        this.registerEventHandler(fxController);
                    }
                }
            }
            if (!(view instanceof PopupView)) {
                if (view.hasStage()) {
                    view.getStage().getIcons().addAll(this.icons);
                }
            }
            theme.initView(view);
            return view;
        } catch (Exception ex) {
            logger.error("fail to init view : " + target.getSimpleName(), ex);
        }
        return null;
    }

    public FXTheme getTheme() {
        return theme;
    }


    @Override
    public void destroy() {
        for (Object view : this.listComponents()) {
            FXView fxView = (FXView)view;
            if (fxView.getLoader() != null) {
                Object controller = fxView.getLoader().getController();
                if (controller instanceof FXController) {
                    FXController fxController = (FXController)controller;
                    fxController.destroy();
                }
            }
        }
        super.destroy();
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return FXView.class.isAssignableFrom(clazz);
    }
}
