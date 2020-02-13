package org.swdc.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;
import org.swdc.fx.services.ServiceManager;
import org.swdc.fx.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Application基类，
 * 提供JavaFXApplication的运行框架，
 * 负责启动，关闭，重启动整个APP。
 *
 * 启动的时候直接使用launch方法即可。
 *
 * Application需要一个SFXApplication注解用来提供必要的
 * 信息，因此请在FXApplication的子类中标注SFXApplication注解。
 *
 */
public abstract class FXApplication extends Application {

    private ApplicationContainer containers;

    private FXSplash splash;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 在JavaFX启动的时候调用，这里开始初始化容器和模块，引导整个APP
     * 的启动。
     * @throws Exception
     */
    public void init() throws Exception{
        InputStream bannerInput = this.getClass().getModule().getResourceAsStream("/banner.txt");
        if (bannerInput == null) {
            bannerInput = FXApplication.class.getModule().getResourceAsStream("banner.txt");
        }
        String banner = Util.readStreamAsString(bannerInput);
        System.out.println(banner);
        bannerInput.close();
        logger.info(" Application initializing..");

        containers = new ApplicationContainer(this);

        ConfigManager configManager = containers.getComponent(ConfigManager.class);
        SFXApplication application = this.getClass().getAnnotation(SFXApplication.class);
        configManager.setAssetsPath(application.assetsPath());

        logger.info(" on launch..");
        this.onLaunch(configManager);
        DefaultUIConfigProp prop = configManager.getOverrideableProperties(DefaultUIConfigProp.class);

        if (prop == null) {
            logger.error(" can not load configuration file, application will stop");
            throw new RuntimeException("配置读取失败。");
        }

        splash = application.splash().getConstructor().newInstance();
        logger.info(" loading splash");
        File splashFile = new File(configManager.getAssetsPath() + "/image/splash.png");
        if (!splashFile.exists()) {
            splashFile = new File(configManager.getAssetsPath() + "/image/splash.jpg");
        }
        if (!splashFile.exists()) {
            logger.warn(" splash image not found.");
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

    /**
     * 此方法在JavaFX的UI线程运行，在这里初始化view，
     * 显示splash，创建ui控件等。
     *
     * 他会注册必要的子容器，框架的设计是外层一个Application的容器
     * 包含很多子容器，子容器按照角色的不同分为Service，View，Config等
     * 通过getComponent可以使用它初始化或获取一个已存在的组件。
     *
     * 这个方法同样负责引导拓展模块，加载拓展模块
     * 进入容器后，拓展模块将会在容器内被激活。这不是自动的，容器
     * 需要手动对组件调用activeExtras方法，让拓展模块生效。
     *
     * @param stage 默认的参数，其实并没有用到
     * @throws Exception
     */
    @Override
    public void start(Stage stage) throws Exception {
        if (splash != null) {
            splash.getStage().show();
        }
        CompletableFuture.runAsync(() -> {
            this.registerManagerContainers();
            this.loadExtraModules();
            logger.info(" on start");
            this.onStart();
        }).whenCompleteAsync((v,e) -> {
            if (e != null) {
                logger.error("exception when loading application:",e);
            }
            Platform.runLater(() -> {
                ViewManager viewManager = containers.getComponent(ViewManager.class);
                SFXApplication application = this.getClass().getAnnotation(SFXApplication.class);
                FXView view = viewManager.getComponent(application.mainView());
                logger.info("application started.");
                if (splash != null) {
                    splash.getStage().close();
                }
                view.show();
            });
        });
    }

    /**
     * 终止Application的运行
     */
    public void stopAndDestroy() {
        logger.info("application is stopping...");
        containers.destroy();
    }

    /**
     * 重启Application
     */
    public void relaunch() {
        Platform.runLater(() -> {
            try {
                this.stopAndDestroy();
                this.init();
                this.start(new Stage());
            } catch (Exception ex) {
                logger.error("error when restart application: ", ex);
            }
        });

    }

    /**
     * 在JavaFX平台关闭的时候会调用，结束Application的运行
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        this.stopAndDestroy();
        logger.info("application has stopped.");
    }

    /**
     * 注册必要的子容器
     * 目前有拓展模块管理器，视图管理器和服务管理器。
     * 子容器可以通过拓展模块增加。
     */
    protected void registerManagerContainers() {
        containers.register(ExtraManager.class);
        containers.register(ViewManager.class);
        containers.register(ServiceManager.class);
    }

    /**
     * 扫描载入的JPMS模块，寻找拓展模块并注册
     */
    private void loadExtraModules() {

        ExtraManager extraManager = containers.getComponent(ExtraManager.class);

        ModuleLayer layer = ModuleLayer.boot();
        Set<Module> modules = layer.modules();
        for (Module mod: modules) {
            try (InputStream in = mod.getResourceAsStream("extra.properties")){
                if (in != null) {
                    Properties properties = new Properties();
                    properties.load(in);
                    String extraClazz = properties.getProperty("module");
                    Class clazz = mod.getClassLoader().loadClass(extraClazz);
                    if (ExtraModule.class.isAssignableFrom(clazz)) {
                        extraManager.register(clazz);
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

    /**
     * 启动的时候调用，这里可以注入自己的Properties，
     * 也可以修改默认的Properties等，这个时候Application
     * 还没有进入真正的启动环节，因此修改后的properties会直接
     * 起效。
     * @param configManager
     */
    protected void onLaunch(ConfigManager configManager){

    }

    /**
     * Application完成初始化但是没有
     * 启动任何界面的时候会调用
     */
    protected void onStart(){

    }

}
