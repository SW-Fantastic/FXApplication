package org.swdc.fx.properties;

import org.controlsfx.property.editor.PropertyEditor;
import org.swdc.fx.AppComponent;
import org.swdc.fx.anno.ConfigProp;
import org.swdc.fx.anno.ConfigProperty;

public class AbstractPropEditor {

    private ConfigProperty prop;

    private AppComponent parent;

    public AbstractPropEditor(ConfigProperty prop, AppComponent component) {
        this.prop = prop;
        this.parent = component;
    }

    public AppComponent getParent() {
        return parent;
    }

    public ConfigProperty getProp() {
        return prop;
    }

    protected PropertyEditor createEditor() {
        return null;
    }

}
