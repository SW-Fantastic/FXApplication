package org.swdc.fx.properties;

import org.swdc.fx.anno.Properties;

@Properties(value = "config.properties", prefix = "app")
public abstract class DefaultUIConfigProp extends FXProperties {

    private String theme;

    private String language = System.getProperty("user.language");

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
