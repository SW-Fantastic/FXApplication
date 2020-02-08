package org.swdc.fx;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;

import java.io.File;
import java.io.FileInputStream;

public abstract class FXApplication extends Application {

    private ViewManager viewManager;

    private ConfigManager configManager;

    private FXSplash splash;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void init() throws Exception{

        logger.info("Application initializing..");

        configManager = new ConfigManager();
        viewManager = new ViewManager(configManager);

        SFXApplication application = this.getClass().getAnnotation(SFXApplication.class);

        configManager.setAssetsPath(application.assetsPath());

        logger.info("on launch..");

        this.onLaunch(configManager);

        DefaultUIConfigProp prop = configManager.getOverrideableProperties(DefaultUIConfigProp.class);

        if (prop == null) {
            logger.error("can not load configuration file, application will stop");
            throw new RuntimeException("配置读取失败。");
        }

        splash = application.splash().getConstructor().newInstance();
        logger.info("loading splash");
        File splashFile = new File(configManager.getAssetsPath() + "/image/splash.png");
        if (!splashFile.exists()) {
            splashFile = new File(configManager.getAssetsPath() + "/image/splash.jpg");
        }
        if (!splashFile.exists()) {
            logger.warn("splash image not found.");
            splash = null;
        }
        if (splash != null) {
            try (FileInputStream inputStream = new FileInputStream(splashFile)){
                splash.setImage(new Image(inputStream));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        if (splash != null) {
            splash.getStage().show();
        }
        logger.info("on start");
        this.onStart();
        splash.getStage().close();
        SFXApplication application = this.getClass().getAnnotation(SFXApplication.class);
        FXView view = viewManager.getView(application.mainView());
        logger.info("application started.");
        view.show();
    }

    protected abstract void onLaunch(ConfigManager configManager);

    protected abstract void onStart();

}
