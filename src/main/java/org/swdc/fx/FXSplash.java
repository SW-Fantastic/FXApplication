package org.swdc.fx;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 欢迎界面，默认是一个图片。
 * 图片从Assets的image文件夹下加载splash.png或者splash.jpg
 * 如果不需要特别的欢迎界面，可以直接使用本类。
 */
public class FXSplash {

    private Image image;

    private BorderPane container;

    private Stage stage;

    /**
     * 创建一个显示欢迎界面图片的窗口，
     * 如果有的话直接返回，需要自定义界面请重写这个。
     * @return
     */
    public Stage getStage() {
        if (stage != null) {
            return stage;
        }
        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        container = new BorderPane();
        ImageView imageView = new ImageView(image);
        container.setCenter(imageView);
        container.setStyle("-fx-background:transparent;");
        Scene scene = new Scene(container);
        scene.setFill(null);
        stage.setScene(scene);
        return stage;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
