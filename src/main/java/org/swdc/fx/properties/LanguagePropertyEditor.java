package org.swdc.fx.properties;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.controlsfx.property.editor.PropertyEditor;
import org.swdc.fx.AppComponent;
import org.swdc.fx.anno.ConfigProperty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguagePropertyEditor extends AbstractPropEditor {

    private ObservableList<Language> languages = FXCollections.observableArrayList();

    public LanguagePropertyEditor(ConfigProperty prop, AppComponent component) {
        super(prop, component);
        String assetsPath = component.getAssetsPath();
        File languages = new File(assetsPath + "/lang.json");
        if (!languages.exists()) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class,String.class,Language.class);
            Map<String,Language> languageMap = mapper.readValue(languages,mapType);
            this.languages.addAll(languageMap.values());
        } catch (Exception e) {
        }
    }

    @Override
    protected PropertyEditor createEditor() {

        ComboBox<Language> comboBox = new ComboBox<>();
        comboBox.setItems(this.languages);
        comboBox.setPrefHeight(28);
        comboBox.getStyleClass().add("comb");
        comboBox.setConverter(new StringConverter<Language>() {
            @Override
            public String toString(Language object) {
                if (object == null) {
                    return null;
                }
                return object.getDisplayName();
            }

            @Override
            public Language fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (Language language: languages) {
                    if (language.getDisplayName().equals(string)) {
                        return language;
                    }
                }
                return null;
            }
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                getProp().setValue(newValue.getName());
            }
        });
        return new PropertyEditor() {
            @Override
            public Node getEditor() {
                return comboBox;
            }

            @Override
            public Object getValue() {
                return comboBox.getSelectionModel().getSelectedItem().toString();
            }

            @Override
            public void setValue(Object value) {
                for (Language language: languages) {
                    if (language.getName().equals(value)) {
                        comboBox.getSelectionModel().select(language);
                    }
                }
            }
        };
    }
}
