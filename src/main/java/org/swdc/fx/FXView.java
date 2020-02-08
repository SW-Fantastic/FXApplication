package org.swdc.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.View;
import org.swdc.fx.properties.ConfigManager;
import org.swdc.fx.properties.FXProperties;

public class FXView {

    private Stage stage;

    private FXMLLoader loader;

    private Parent parent;

    private ConfigManager configManager;

    private ViewManager viewManager;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public FXView() {
        try {
            stage = new Stage();
            View view = this.getClass().getAnnotation(View.class);
            FXMLLoader loader = new FXMLLoader();
            String path = view.value();
            if (path.equals("")) {
                path = "views/" + getClass().getSimpleName() + ".fxml";
            }
            parent = loader.load(this.getClass().getModule().getResourceAsStream(path));
            Scene scene = new Scene(parent);

            stage.initStyle(view.stageStyle());
            stage.setScene(scene);
            stage.setTitle(view.title());
            stage.setResizable(view.resizeable());

            this.loader = loader;
            this.onInitialized();
        } catch (Exception ex) {
            logger.error("error when load fxml view",ex);
        }
    }

    protected void onInitialized() {

    }

    public Stage getStage() {
        return stage;
    }

    public void show() {
        if (stage.isShowing()) {
            stage.requestFocus();
        } else {
            stage.showAndWait();
        }
    }

    public <T extends FXView> T findView(Class<T> view) {
        return viewManager.getView(view);
    }

    public <T extends FXProperties> T findProperties(Class<T> config) {
        return configManager.getOverrideableProperties(config);
    }

    protected void setManagers(ViewManager viewManager, ConfigManager configManager) {
        this.viewManager = viewManager;
        this.configManager = configManager;
    }

    public FXMLLoader getLoader() {
        return loader;
    }

    public Parent getView() {
        return parent;
    }
}
