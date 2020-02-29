package org.swdc.fx;

import javafx.scene.Node;
import org.swdc.fx.anno.View;

import java.io.File;

/**
 * 主题，这个在DefaultProp里面进行配置
 * 资源文件默认会从Assets文件夹获取。
 */
public class FXTheme {

    private String name;
    private String assetsPath;

    /**
     * 定义一个主题
     * @param name 主题名，对应Asset路径 - theme文件夹下的主题名
     * @param assetsPath assets文件夹的路径
     */
    public FXTheme(String name, String assetsPath) {
        this.name = name;
        this.assetsPath = assetsPath;
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
        if (viewProp.style().length == 1 && viewProp.style()[0].equals("")) {
            if(viewProp.stage()) {
                view.getStage()
                        .getScene()
                        .getStylesheets()
                        .add(new File(this.assetsPath + "/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
                File background = new File(this.assetsPath + "/theme/" + this.name + "/background.jpg");
                if (!background.exists()) {
                    background = new File(this.assetsPath + "/theme/" + this.name + "/background.png");
                }
                if (background.exists() && viewProp.background()) {
                    view.getStage()
                            .getScene()
                            .getRoot()
                            .setStyle("-fx-background-image: url(" + background.toURI().toURL().toExternalForm() + ");");
                }
            } else {
                view.getView()
                        .getStylesheets()
                        .add(new File(this.assetsPath + "/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
            }
        } else {
            String[] themes = viewProp.style();
            for (String style: themes) {
                if (viewProp.stage()) {
                    view.getStage()
                            .getScene()
                            .getStylesheets()
                            .add(new File(this.assetsPath + "/theme/" + style + ".css").toURI().toURL().toExternalForm());
                } else {
                    view.getView()
                            .getStylesheets()
                            .add(new File(this.assetsPath + "/theme/" + style + ".css").toURI().toURL().toExternalForm());
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
