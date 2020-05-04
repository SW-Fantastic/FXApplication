package org.swdc.fx.properties;

import javafx.scene.Node;
import org.controlsfx.control.PropertySheet;
import org.swdc.fx.AppComponent;

public abstract class FXProperties extends AppComponent {

    private PropertiesResolver resolver = null;

    public void setResolver(PropertiesResolver resolver) {
        this.resolver = resolver;
    }

    private Node editor;

    public Node getEditor(){
        if (editor != null) {
            return editor;
        }
        try {
            PropertySheet propertySheet = new PropertySheet(PropertyEditors.getProperties(this));
            propertySheet.setPropertyEditorFactory(item -> PropertyEditors.getEditor(item, this));
            propertySheet.setModeSwitcherVisible(false);
            propertySheet.getStyleClass().add("prop-sheets");
            editor = propertySheet;
            return propertySheet;
        } catch (Exception e) {
            return null;
        }
    }

    public void saveProperties() {
        if (this.resolver == null) {
            return;
        }
        this.resolver.saveProperties(this);
    }

}
