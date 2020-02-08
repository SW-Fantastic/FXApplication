package org.swdc.fx;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FXSplash {

    private Image image;

    private BorderPane container;

    private Stage stage;

    public Stage getStage() {
        if (stage != null) {
            return stage;
        }
        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        container = new BorderPane();
        ImageView imageView = new ImageView(image);
        container.setCenter(imageView);
        Scene scene = new Scene(container);
        stage.setScene(scene);
        return stage;
    }

    public BorderPane getContainer() {
        return container;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
