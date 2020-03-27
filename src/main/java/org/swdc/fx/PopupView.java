package org.swdc.fx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.swdc.fx.anno.View;
import org.swdc.fx.util.Util;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Optional;

/**
 * 弹窗型窗口，无任务栏图标，
 * 适合桌面的弹窗，菜单类型的小弹窗。
 *
 * 强制使用JWindow，而不是Stage，无标题栏，View注解属性关于Stage
 * 的这里会失效。
 */
public class PopupView extends FXView {

    private Parent parent;

    private JFXPanel jfxPanel;

    private FXMLLoader loader;

    private JWindow window = new JWindow();

    @Override
    public void initialize() {

    }

    /**
     * 加载FXML，以获取View
     * 将会从View注解中value的位置读取，如果没有的话，
     * 就会尝试使用create方法创建View。
     * @return
     */
    protected boolean loadFxmlView() {
        try {
            View view = this.getClass().getAnnotation(View.class);
            FXMLLoader loader = new FXMLLoader();
            String path = view.value();
            if (path.equals("")) {
                path = "views/" + getClass().getSimpleName() + ".fxml";
            }
            try(InputStream inputStream = this.getClass().getModule().getResourceAsStream(path)){
                if (inputStream != null) {
                    parent = loader.load(inputStream);
                    parent.setUserData(this);
                    Scene scene = new Scene(parent);
                    jfxPanel = new JFXPanel();
                    jfxPanel.setScene(scene);
                    window.add(jfxPanel);
                    window.setSize((int)scene.getWidth(),(int)scene.getHeight());
                    window.setAlwaysOnTop(true);
                    this.loader = loader;
                    return true;
                }
                return false;
            } catch (Exception ex) {
                logger.error("error when load fxml view",ex);
                return false;
            }
        } catch (Exception ex) {
            logger.error("error when load fxml view",ex);
            return false;
        }
    }

    @Override
    protected void createView() {
        View view = this.getClass().getAnnotation(View.class);
        this.parent = create();
        window = new JWindow();
        window.setName(view.title());

        Scene scene = new Scene(parent);
        jfxPanel = new JFXPanel();
        jfxPanel.setScene(scene);

        window.setSize((int)scene.getWidth(),(int)scene.getHeight());
        window.setAlwaysOnTop(true);
        window.add(jfxPanel);
        if (view.dialog()) {
            window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        }
    }

    @Override
    public void show() {
       window.setVisible(true);
    }

    @Override
    public void close() {
        if (window.isVisible()) {
            window.setVisible(false);
        }
    }

    @Override
    public FXMLLoader getLoader() {
        return loader;
    }

    public JWindow getSWingStage(){
        return window;
    }

    @Override
    public <T> T getView() {
        return (T)this.jfxPanel.getScene().getRoot();
    }

    @Override
    public <T> T findById(String id) {
        return super.findById(id);
    }

    @Override
    public Optional<ButtonType> showAlertDialog(String title, String content, Alert.AlertType type) {
        return Util.showAlertDialog(null,content,title,type,this.findTheme());
    }
}
