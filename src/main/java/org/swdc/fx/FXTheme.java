package org.swdc.fx;

import com.asual.lesscss.LessEngine;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.View;

import java.io.File;

/**
 * 主题，这个在DefaultProp里面进行配置
 * 资源文件默认会从Assets文件夹获取。
 */
public class FXTheme {

    private String name;
    private String assetsPath;

    private static Logger logger = LoggerFactory.getLogger(FXTheme.class);

    /**
     * 定义一个主题
     * @param name 主题名，对应Asset路径 - theme文件夹下的主题名
     * @param assetsPath assets文件夹的路径
     */
    public FXTheme(String name, String assetsPath) {
        this.name = name;
        this.assetsPath = assetsPath;

        File assets = new File(this.assetsPath + "/theme/" + this.name );
        File[] files = assets.listFiles();
        try {
            for (File item : files) {
                if (item.isFile() && item.getName().endsWith("less")) {
                    String cssName = item.getName().replace("less", "css");
                    File css = new File(item.getParent() + File.separator + cssName);
                    if (!css.exists()) {
                        LessEngine lessEngine = new LessEngine();
                        lessEngine.compile(item,css);
                        logger.info("compile style file :" + this.name);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("fail to compile style source.");
        }
    }

    public String getName() {
        return name;
    }

    /**
     * 对FXView配置主题样式
     * @param view 配置中的View
     * @throws Exception url异常，一般不会出现
     */
    public void initView(FXView view) throws Exception {
        View viewProp = view.getClass().getAnnotation(View.class);
        File background = new File(this.assetsPath + "/theme/" + this.name + "/background.jpg");
        if (!background.exists()) {
            background = new File(this.assetsPath + "/theme/" + this.name + "/background.png");
        }
        if (viewProp.style().length == 1 && viewProp.style()[0].equals("")) {
            if(viewProp.stage()) {
                view.getStage()
                        .getScene()
                        .getStylesheets()
                        .add(new File(this.assetsPath + "/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
                if (background.exists() && viewProp.background()) {
                    view.getStage()
                            .getScene()
                            .getRoot()
                            .setStyle("-fx-background-image: url(" + background.toURI().toURL().toExternalForm() + ");");
                }
            } else {
                ((Parent)view.getView())
                        .getStylesheets()
                        .add(new File(this.assetsPath + "/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
            }
        } else {
            String[] themes = viewProp.style();
            if (viewProp.stage()) {
                view.getStage()
                        .getScene()
                        .getStylesheets()
                        .add(new File(this.assetsPath + "/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
            }

            if (background.exists() && viewProp.background()) {
                view.getStage()
                        .getScene()
                        .getRoot()
                        .setStyle("-fx-background-image: url(" + background.toURI().toURL().toExternalForm() + ");");
            }
            for (String style: themes) {
                if (viewProp.stage()) {
                    view.getStage()
                            .getScene()
                            .getStylesheets()
                            .add(new File(this.assetsPath + "/theme/" + this.name + "/" + style + ".css").toURI().toURL().toExternalForm());
                } else {
                    ((Parent)view.getView())
                            .getStylesheets()
                            .add(new File(this.assetsPath + "/theme/" + this.name + "/" + style + ".css").toURI().toURL().toExternalForm());
                }

            }
        }
    }

    public void initFXView(Node node) {
        try {
            node.getScene()
                    .getStylesheets()
                    .add(new File(this.assetsPath + "/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
        } catch (Exception e) {

        }
    }

}
