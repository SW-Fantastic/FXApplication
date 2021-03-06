package org.swdc.fx;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.container.Container;
import org.swdc.fx.event.AppEvent;
import org.swdc.fx.extra.ExtraLoader;
import org.swdc.fx.net.data.MainParameter;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.net.ApplicationService;
import org.swdc.fx.net.data.ExternalMessage;
import org.swdc.fx.net.MainParamHandler;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.DefaultUIConfigProp;
import org.swdc.fx.services.ServiceManager;
import org.swdc.fx.util.Util;

import java.awt.*;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
public abstract class FXApplication extends Application implements OpenFilesHandler {

    /**
     * 同步对象，需要同步的时候充当锁使用
     */
    private static final Object syncObject = new Object();

    /**
     * 应用主容器
     */
    private ApplicationContainer containers;

    /**
     * 闪屏界面
     */
    private FXSplash splash;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private Boolean hasStopped = true;

    /**
     * 应用服务，将会用来单例启动、
     */
    private ApplicationService service;

    /**
     * 在JavaFX启动的时候调用，这里开始初始化容器和模块，引导整个APP
     * 的启动。
     * @throws Exception
     */
    public void init() throws Exception {
        synchronized (syncObject) {
            hasStopped = false;
            InputStream bannerInput = this.getClass().getModule().getResourceAsStream("banner.txt");
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

            if (application.singleton()) {
                service = new ApplicationService(this);
                if (!service.startUp()) {
                    logger.warn("application has started, invoking existed.");
                    // 无法启动service，说明存在另一个应用
                    // 标注为singleton，即单例起动，停止本实例启动过程
                    // 并且发送启动参数到已知实例。
                    List<String> params = getParameters().getRaw();
                    MainParameter parameter = new MainParameter(params.toArray(new String[0]));
                    ObjectMapper mapper = new ObjectMapper();
                    byte[] data = mapper.writeValueAsBytes(parameter);
                    ExternalMessage message = new ExternalMessage(data, MainParameter.class);
                    service.pushMessage(message);
                    System.exit(0);
                }
                service.addListener(new MainParamHandler());
                if (Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
                    Desktop.getDesktop().setOpenFileHandler(this);
                }
            }

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
            splash.getStage().getIcons().addAll(this.loadIcons());
            splash.getStage().show();
        }
        CompletableFuture.runAsync(() -> {
            synchronized (syncObject) {
                this.registerManagerContainers();
                this.loadExtraModules();
                logger.info(" on start");
                this.onStart(containers);
            }
        }).whenCompleteAsync((v,e) -> {
            if (e != null) {
                logger.error("exception when loading application:",e);
                return;
            }
            Platform.runLater(() -> {
                ViewManager viewManager = containers.getComponent(ViewManager.class);
                SFXApplication application = this.getClass().getAnnotation(SFXApplication.class);
                FXView view = viewManager.getComponent(application.mainView());
                logger.info("application started.");
                if (splash != null) {
                    splash.getStage().close();
                }
                this.appHasStarted(view);
                List<String> params = this.getParameters().getUnnamed();
                if (params.size() > 0) {
                    String path = params.get(0);
                    File file = new File(path);
                    if (file.isFile() && file.exists()) {
                        this.onFileOpenRequest(file);
                    }
                }
            });
        });
    }

    protected void appHasStarted(FXView mainView) {
        mainView.show();
    }

    protected void onStop(ApplicationContainer container){

    }

    /**
     * 终止Application的运行
     */
    public void stopAndDestroy() {
        synchronized (syncObject) {
            try {
                if (hasStopped) {
                    return;
                }
                logger.info("on stop");
                this.onStop(containers);
                logger.info("application is stopping...");
                if (service != null) {
                    service.shutdown();
                }
                containers.destroy();
                logger.info("application has stopped.");
                System.exit(0);
            } catch (Exception ex) {
                logger.error("fail to stop application",ex);
            }
        }
    }

    @Override
    public void openFiles(OpenFilesEvent e) {
        if (e.getFiles().size() == 1) {
            this.onFileOpenRequest(e.getFiles().get(0));
        } else {
            for (File file: e.getFiles()) {
                this.onFileOpenRequest(file);
            }
        }
    }

    /**
     * 来自文件关联的处理，重写此方法以处理
     * 被关联的文件的打开。
     * @param file
     */
    public void onFileOpenRequest(File file) {

    }

    /**
     * 在JavaFX平台关闭的时候会调用，结束Application的运行
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        this.stopAndDestroy();
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

        ServiceLoader<ExtraLoader> loaders = ServiceLoader.load(ExtraLoader.class);
        loaders.stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparingInt(ExtraLoader::getOrder))
                .map(ExtraLoader::getModuleClass)
                .forEach(extraManager::register);
        
    }

    public <T> List<T> getScoped(Class<T> scopeClazz) {
        List<Container> containers = this.containers.listComponents();
        for (Container container: containers) {
            if (container.isComponentOf(scopeClazz)) {
                List<Object> content = container.listComponents();
                List<T> result = new ArrayList<>();
                for (Object comp: content){
                    if (scopeClazz.isAssignableFrom(comp.getClass())) {
                        result.add((T)comp);
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
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
    protected void onStart(ApplicationContainer container){

    }

    protected List<Image> loadIcons() {
        try {

            Module module = FXApplication.class.getModule();

            ArrayList<Image> icons = new ArrayList<>();
            icons.add(new Image(module.getResourceAsStream("icons/iconx16.png")));
            icons.add(new Image(module.getResourceAsStream("icons/iconx24.png")));
            icons.add(new Image(module.getResourceAsStream("icons/iconx32.png")));
            icons.add(new Image(module.getResourceAsStream("icons/iconx48.png")));
            icons.add(new Image(module.getResourceAsStream("icons/iconx64.png")));
            return icons;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public <T> T findComponent(Class<T> clazz) {
        List<Container> containerList = containers.listComponents();
        for (Container container: containerList) {
            if (container.isComponentOf(clazz)) {
                return (T)container.getComponent(clazz);
            }
        }

        ExtraManager extraManager = containers.getComponent(ExtraManager.class);
        List<ExtraModule> modules = extraManager.listComponents();
        for (ExtraModule module : modules) {
            if (module.isComponentOf(clazz)) {
                return (T)module.getComponent(clazz);
            }
        }
        return null;
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
        List<Container> containers = this.containers.listComponents();
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

    public <T> List<T> findAllScopedComponent(Class<T> clazz, Predicate<T> condition) {
        List<Container> containerList = containers.listComponents();
        List target = null;
        for (Container container: containerList) {
            if (!container.isComponentOf(clazz)) {
                continue;
            }
            target = container.findAllScopedComponent(clazz,condition);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    public void emit(AppEvent event) {
        containers.emit(event);
    }

}
