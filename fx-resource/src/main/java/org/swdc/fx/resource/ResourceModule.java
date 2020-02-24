package org.swdc.fx.resource;

import javafx.scene.text.Font;
import org.swdc.fx.*;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.extra.ExtraModule;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 搜寻和加载字体，图标字体，以及搜寻各类资源
 */
public class ResourceModule extends ExtraModule {

    private SFXApplication applicationDesc;

    private HashMap<Class, Object> instanceMap = new HashMap<>();

    @Override
    public Object getComponent(Class clazz) {
        if (!isComponentOf(clazz)) {
            return null;
        }
        if (instanceMap.containsKey(clazz)) {
            return instanceMap.get(clazz);
        } else {
            return register(clazz);
        }
    }

    @Override
    public Object register(Class clazz) {
        if (ResourceService.class.isAssignableFrom(clazz)) {
            try {
                Object rs = clazz.getConstructor().newInstance();
                ResourceService service = ResourceService.class.cast(rs);
                service.setAssetsPath(applicationDesc.assetsPath());
                service.initialize();
                instanceMap.put(clazz, service);
                return service;
            } catch (Exception ex) {
                logger.error("fail instance resource service", ex);
                return null;
            }
        } else {
            try {
                IconFontService iconFontService = (IconFontService) clazz.getConstructor().newInstance();
                iconFontService.initialize();
                instanceMap.put(clazz, iconFontService);
                return iconFontService;
            } catch (Exception ex) {
                logger.error("fail to instance icon service: ", ex);
                return null;
            }
        }
    }

    @Override
    public List<Object> listComponents() {
        return new ArrayList<>(instanceMap.values());
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return IconFontService.class.isAssignableFrom(clazz) ||
                ResourceService.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean initialize(ApplicationContainer container) {
        Object app = container.getApplication();
        applicationDesc = app.getClass().getAnnotation(SFXApplication.class);
        resolveFonts(container);
        return true;
    }

    private void resolveFonts(ApplicationContainer container) {
        ViewManager viewManager = container.getComponent(ViewManager.class);
        FXTheme theme = viewManager.getTheme();
        String environment = applicationDesc.assetsPath() + File.separator + "theme" + File.separator + theme.getName();
        File themeFolder = new File(environment);
        File[] files = themeFolder.listFiles();
        if (files != null) {
            for (File file: files) {
                if (file.getName().endsWith("ttf") || file.getName().endsWith("otf")) {
                    try {
                        Font font = Font.loadFont(new FileInputStream(file), 16);
                        logger.info("loaded font : " + font.getFamily());
                    } catch (Exception ex) {
                        logger.error("can not load font :" + file.getName());
                    }
                }
            }
        } else {
            logger.error("can not read theme folder: " + theme.getName());
        }

        File commonsFont = new File(applicationDesc.assetsPath() + File.separator + "fonts");
        if(commonsFont.exists()) {
            File[] commonFonts = commonsFont.listFiles();
            if (commonFonts != null) {
                for (File cFont: commonFonts) {
                    try {
                        Font commFont = Font.loadFont(new FileInputStream(cFont),16);
                        logger.info("font " + commFont.getFamily() + " loaded.");
                    } catch (Exception ex) {
                        logger.error("can not load font :" + cFont.getName(), ex);
                    }
                }
            }
        }
    }

    @Override
    public boolean destroy(ApplicationContainer container) {
        for (Object obj: instanceMap.values()) {
            LifeCircle lifeCircle = (LifeCircle) obj;
            lifeCircle.destroy();
        }
        return true;
    }

    @Override
    public boolean support(Class container) {
        return false;
    }

    @Override
    public void activeOnComponent(Object comp) {

    }

    @Override
    public void disposeOnComponent(Object comp) {

    }
}
