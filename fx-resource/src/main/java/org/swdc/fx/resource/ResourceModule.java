package org.swdc.fx.resource;

import javafx.scene.text.Font;
import org.swdc.fx.*;
import org.swdc.fx.anno.SFXApplication;
import org.swdc.fx.extra.ExtraModule;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * 搜寻和加载字体，图标字体，以及搜寻各类资源
 */
public class ResourceModule extends ExtraModule<AppComponent> {
    @Override
    public <R extends AppComponent> R getComponent(Class<R> clazz) {
        return null;
    }

    @Override
    public <R extends AppComponent> IconFontService register(Class<R> clazz) {
        return null;
    }

    @Override
    public <R extends AppComponent> List<R> listComponents() {
        return null;
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return IconFontService.class.isAssignableFrom(clazz) ||
                ResourceService.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean initialize(ApplicationContainer container) {
        resolveFonts(container);
        return true;
    }

    private void resolveFonts(ApplicationContainer container) {
        Object app = container.getApplication();
        SFXApplication applicationDesc = app.getClass().getAnnotation(SFXApplication.class);

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
        return false;
    }

    @Override
    public <T extends Container> boolean support(Class<T> container) {
        return false;
    }

    @Override
    public void activeOnComponent(AppComponent comp) {

    }

    @Override
    public void disposeOnComponent(AppComponent comp) {

    }
}
