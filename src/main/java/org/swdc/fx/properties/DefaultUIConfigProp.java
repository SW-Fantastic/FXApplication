package org.swdc.fx.properties;

import org.swdc.fx.anno.Properties;

@Properties(value = "config.properties", prefix = "app")
public class DefaultUIConfigProp extends FXProperties {

    private String theme;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
