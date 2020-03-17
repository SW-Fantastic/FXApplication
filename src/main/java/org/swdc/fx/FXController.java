package org.swdc.fx;

import javafx.fxml.Initializable;

/**
 * 默认的Controller
 * 如果继承这个controller，并且在fxml中指定，那么
 * 此controller将会在viewManager被注入一些环境相关的内容
 * 因此他可以调用findXXX获取组件。
 */
public abstract class FXController extends AppComponent implements Initializable {

    private FXView view;

    public void setView(FXView view) {
        this.view = view;
    }

    public <T> T getView() {
        return (T)view;
    }
}
