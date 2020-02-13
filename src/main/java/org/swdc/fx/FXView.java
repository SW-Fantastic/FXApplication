package org.swdc.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.View;

import java.io.InputStream;

/**
 * 描述一个view的类。
 * 请继承他以添加View或者窗口。
 *
 * 本类需要一个View注解，请在他的子类中标注。
 * 如果没有在view注解中指定value，那么将会在 resources/views中查找
 * 和类名相同的fxml文件，因此需要在module-info中让view对本框架open
 *
 * 如果找不到FXML，将会尝试使用create方法创建view，请在此方法返回Parent。
 *
 */
public class FXView extends AppComponent{

    private Stage stage;

    private FXMLLoader loader;

    private Parent parent;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 加载FXML，以获取View
     * 将会从View注解中value的位置读取，如果没有的话，
     * 就会尝试使用create方法创建View。
     * @return
     */
    protected final boolean loadFxmlView() {
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
                    Scene scene = new Scene(parent);
                    if (view.stage()) {
                        stage = new Stage();
                        stage.initStyle(view.stageStyle());
                        stage.setTitle(view.title());
                        stage.setResizable(view.resizeable());
                        stage.setScene(scene);
                    }
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

    /**
     * 创建组件，如果准备使用编程实现ui，请不要创建fxml文件并重写此方法。
     * @return
     */
    protected Parent create() {
        return null;
    }

    /**
     * 销毁view，一般是在Application退出的时候自动调用
     */
    @Override
    public void destroy() {
        if (this.stage != null && this.stage.isShowing()) {
            this.stage.close();
        }
    }

    /**
     * ViewManager会使用此方法，尝试通过Create创建View。
     */
    protected final void createView() {
        View view = this.getClass().getAnnotation(View.class);
        this.parent = create();
        if (view.stage()) {
            stage = new Stage();
            stage.initStyle(view.stageStyle());
            stage.setTitle(view.title());
            stage.setResizable(view.resizeable());
            stage.setScene(new Scene(parent));
        }
    }

    /**
     * 获取Stage，如果在View注解中将Stage配置为false，
     * 并且使用此方法，会出现一个异常。
     * @return
     */
    public Stage getStage() {
        View view = this.getClass().getAnnotation(View.class);
        if (!view.stage()) {
            logger.error(" view " + getClass().getSimpleName() + " has no stage.");
            return null;
        }
        return stage;
    }

    /**
     * 显示此view，如果没有stage，则什么都不做。
     */
    public void show() {
        View view = this.getClass().getAnnotation(View.class);
        if (!view.stage()) {
            logger.error(" view " + getClass().getSimpleName() + " can not be show, because it does not have a stage");
            return;
        }
        if (stage.isShowing()) {
            stage.requestFocus();
        } else {
            stage.showAndWait();
        }
    }

    /**
     * 提供获取FXMLLoader的方法
     * @return
     */
    public FXMLLoader getLoader() {
        return loader;
    }

    /**
     * 获取ui对象。
     * @return
     */
    public Parent getView() {
        return parent;
    }
}
