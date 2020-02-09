package org.swdc.fx.properties;

import javafx.scene.Node;
import org.swdc.fx.anno.Properties;

@Properties(value = "config.properties", prefix = "app")
public class DefaultUIConfigProp extends FXProperties {

    private String theme;

    @Override
    public Node getEditor() {
        return null;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
