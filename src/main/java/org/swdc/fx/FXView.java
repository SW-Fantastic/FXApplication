package org.swdc.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.View;

import java.io.InputStream;

public class FXView extends AppComponent{

    private Stage stage;

    private FXMLLoader loader;

    private Parent parent;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected boolean loadFxmlView() {
        try {
            stage = new Stage();
            View view = this.getClass().getAnnotation(View.class);
            FXMLLoader loader = new FXMLLoader();
            String path = view.value();
            if (path.equals("")) {
                path = "views/" + getClass().getSimpleName() + ".fxml";
            }
            try(InputStream inputStream = this.getClass().getModule().getResourceAsStream(path)){
                if (inputStream != null) {
                    parent = loader.load(inputStream);
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.initStyle(view.stageStyle());
                    stage.setTitle(view.title());
                    stage.setResizable(view.resizeable());
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

    protected void onInitialized() {

    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    protected Parent createView() {
        return null;
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

    public FXMLLoader getLoader() {
        return loader;
    }

    public Parent getView() {
        return parent;
    }
}
