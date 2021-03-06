package org.swdc.fx;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.View;
import org.swdc.fx.properties.Languages;
import org.swdc.fx.util.Util;
import org.swdc.fx.ux.MessageView;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

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
public class FXView extends AppComponent {

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
                    this.i18n(parent);
                    if (view.stage()) {
                        Scene scene = new Scene(parent);
                        stage = new Stage();
                        stage.initStyle(view.stageStyle());
                        stage.setTitle(i18n(view.title()));
                        stage.setResizable(view.resizeable());
                        stage.setScene(scene);
                        if (view.dialog()) {
                            stage.initModality(Modality.APPLICATION_MODAL);
                        }
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
     * 处理国际化文本
     * @param node
     */
    private void i18n(Parent node) {

        try {
            try{
                Method textProp = node.getClass().getMethod("textProperty");
                StringProperty prop = (StringProperty) textProp.invoke(node);
                prop.setValue(i18n(prop.getValue()));
            } catch (NoSuchMethodException ignore) {
            }

            try{
                Method textProp = node.getClass().getMethod("promptTextProperty");
                StringProperty prop = (StringProperty) textProp.invoke(node);
                prop.setValue(i18n(prop.getValue()));
            } catch (NoSuchMethodException ignore) {
            }

        } catch (Exception e) {
            logger.error("fail to resolve language",e);
        }

        resolveI18nNode(node);

        if (node.getChildrenUnmodifiable().size() > 0) {
            for (Node item : node.getChildrenUnmodifiable()) {
                if (item instanceof Parent) {
                    Parent next = (Parent)item;
                    if (next.getChildrenUnmodifiable().size() > 0) {
                        i18n(next);
                    } else {
                        resolveI18nNode(next);
                    }
                } else {
                    resolveI18nNode(item);
                }
            }
        }
    }

    /**
     * 处理一些复杂组件的国际化
     * @param item
     */
    private void resolveI18nNode(Node item) {
        if (item instanceof TableView) {
            TableView view = (TableView) item;
            view.getColumns().forEach(col -> {
                TableColumn column = (TableColumn) col;
                column.setText(this.i18n(column.getText()));
            });
            if (view.getPlaceholder() instanceof Parent) {
                this.i18n((Parent) view.getPlaceholder());
            } else {
                this.resolveI18nNode(view.getPlaceholder());
            }
        } else if(item instanceof TabPane) {
            TabPane tabPane = (TabPane) item;
            for (Tab tab:tabPane.getTabs()) {
                tab.setText(this.i18n(tab.getText()));
                Node content = tab.getContent();
                if (content instanceof Parent) {
                    this.i18n((Parent) content);
                } else {
                    this.resolveI18nNode(content);
                }
            }
        } else if (item instanceof SplitPane) {
              SplitPane splitPane = (SplitPane) item;
              for (Node node : splitPane.getItems()) {
                  if (node instanceof Parent) {
                      this.i18n((Parent)node);
                  } else {
                      resolveI18nNode(node);
                  }
              }
        } else if (item instanceof Labeled) {
            Labeled labeled = (Labeled) item;
            labeled.setText(this.i18n(labeled.getText()));
        } else if (item instanceof TextInputControl) {
            TextInputControl inputControl = (TextInputControl) item;
            inputControl.setText(this.i18n(inputControl.getText()));
            if (inputControl.getPromptText() != null) {
                inputControl.setPromptText(inputControl.getPromptText());
            }
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
        Platform.runLater(() -> {
            if (this.stage != null && this.stage.isShowing()) {
                this.stage.close();
            }
        });
    }

    public void showToast(String text) {
        try {
            MessageView msgView = this.findComponent(MessageView.class);
            msgView.setText(text);
            Notifications.create()
                    .graphic(msgView.getView())
                    .position(Pos.CENTER)
                    .hideAfter(Duration.seconds(2))
                    .hideCloseButton()
                    .show();
        } catch (Exception e) {
        }
    }

    /**
     * ViewManager会使用此方法，尝试通过Create创建View。
     */
    protected void createView() {
        View view = this.getClass().getAnnotation(View.class);
        this.parent = create();
        this.parent.setUserData(this);
        if (view.stage()) {
            stage = new Stage();
            stage.initStyle(view.stageStyle());
            stage.setTitle(view.title());
            stage.setResizable(view.resizeable());
            stage.setScene(new Scene(parent));
            if (view.dialog()) {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
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
            if (view.dialog()) {
                stage.showAndWait();
            } else {
                stage.show();
            }
        }
    }

    public void close() {
        View view = this.getClass().getAnnotation(View.class);
        if (!view.stage()) {
            logger.error(" view " + getClass().getSimpleName() + " can not be show, because it does not have a stage");
            return;
        }
        if (stage.isShowing()) {
            stage.close();
        }
    }


    public boolean hasStage() {
        View view = this.getClass().getAnnotation(View.class);
        return view.stage();
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
    public <T> T getView() {
        return (T)parent;
    }

    public <T> T findById(String id) {
        T look = (T) ((Parent)this.getView()).lookup("#" + id);
        if (look != null) {
            return look;
        }
        if (parent instanceof SplitPane) {
            return findById(id,parent);
        }
        List<Node> childs = parent.getChildrenUnmodifiable();
        for (Node node : childs){
            if (id.equals(node.getId())) {
                return (T)node;
            } else {
                T target = findById(id,node);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    private <T> T findById(String id, Node parent) {
        if (parent instanceof ToolBar) {
            ToolBar toolBar = (ToolBar) parent;
            List<Node> tools = toolBar.getItems();
            for (Node item: tools) {
                if (id.equals(item.getId())) {
                    return (T)item;
                }
            }
            return null;
        } else if (parent instanceof SplitPane) {
            SplitPane splitPane = (SplitPane)parent;
            for (Node item: splitPane.getItems()) {
                if (id.equals(item.getId())) {
                    return (T)item;
                } else {
                    Node target = findById(id,item);
                    if (target != null) {
                        return (T) target;
                    }
                }
            }
            return null;
        } else if (parent instanceof ScrollPane) {
          ScrollPane scrollPane = (ScrollPane)parent;
          if (scrollPane.getContent().getId().equals(id)) {
              return (T)scrollPane.getContent();
          } else {
              return findById(id,scrollPane.getContent());
          }
        } else if (parent instanceof Pane) {
            Pane pane = (Pane)parent;
            for (Node node: pane.getChildren()) {
                if (id.equals(node.getId())) {
                    return (T)node;
                } else {
                    Node next = findById(id, node);
                    if (next != null) {
                        return (T)next;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public Optional<ButtonType> showAlertDialog(String title, String content, Alert.AlertType type) {
        if (this.hasStage()) {
            return Util.showAlertDialog(this.getStage(),content,title,type,this.findTheme());
        } else {
            return Util.showAlertDialog(null,content,title,type,this.findTheme());
        }
    }

    public static <T> T fxViewByView(Node parent, Class<T> clazz) {
        if (parent.getUserData() == null) {
            return null;
        }
        return (T)clazz.cast(parent.getUserData());
    }

}
