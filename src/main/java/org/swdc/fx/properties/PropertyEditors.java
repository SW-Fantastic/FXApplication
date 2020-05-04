package org.swdc.fx.properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.AppComponent;
import org.swdc.fx.anno.ConfigProp;
import org.swdc.fx.anno.ConfigProperty;
import org.swdc.fx.anno.PropResolver;
import org.swdc.fx.extra.IconSPIService;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;


public class PropertyEditors {

    private static Logger logger = LoggerFactory.getLogger(PropertyEditors.class);

    public static PropertyEditor createColorEditor(ConfigProperty property){
        HBox hbox = new HBox();
        hbox.setSpacing(8);
        ColorPicker picker = new ColorPicker();
        TextField text = new TextField();

        HBox.setHgrow(picker, Priority.ALWAYS);
        HBox.setHgrow(text, Priority.ALWAYS);
        hbox.getChildren().addAll(picker, text);

        picker.setValue(Color.web(property.getValue().toString()));
        text.setText(property.getValue().toString());

        picker.setPrefHeight(28);
        text.setPrefHeight(28);
        text.getStyleClass().add("txt");

        picker.valueProperty().addListener((observable, oldValue, newValue) -> {
            picker.setValue(newValue);
            String data = "#" + Integer.toHexString(newValue.hashCode());
            if (data.equals("#ff")) {
                data = "#000000";
            }
            text.setText(data.substring(0,7));
            property.setValue(data.substring(0,7));
        });

        picker.getStyleClass().add("comb");

        text.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 6) {
                picker.setValue(Color.web(newValue));
                property.setValue(newValue);
            }
        });

        return new PropertyEditor() {
            @Override
            public Node getEditor() {
                return hbox;
            }

            @Override
            public Object getValue() {
                return text.getText();
            }

            @Override
            public void setValue(Object o) {
                if (o instanceof Color) {
                    String data = "#" + Integer.toHexString(o.hashCode()).substring(0,6);
                    text.setText(data);
                    picker.setValue(Color.web(data));
                } else {
                    text.setText(o.toString());
                    picker.setValue(Color.web(o.toString()));
                }
            }
        };
    }

    public static PropertyEditor<?> createCheckedEditor(ConfigProperty property) {
        CheckBox check = new CheckBox();
        check.setSelected((Boolean)property.getValue());
        check.getStyleClass().add("check");
        check.selectedProperty().addListener((observable, oldValue, newValue) -> {
            property.setValue(newValue);
        });
        check.setPrefHeight(28);
        return new PropertyEditor() {
            @Override
            public Node getEditor() {
                return check;
            }

            @Override
            public Object getValue() {
                return check.isSelected();
            }

            @Override
            public void setValue(Object o) {
                check.setSelected((Boolean)o);
            }
        };
    }

    public static ObservableList<PropertySheet.Item> getProperties(Object object) throws Exception {
        ObservableList<PropertySheet.Item> list = FXCollections.observableArrayList();
        Field[] fields = object.getClass().getDeclaredFields();
        for(Field field: fields) {
            if (field.getAnnotation(ConfigProp.class) == null){
                continue;
            }
            ConfigProp propDefinition = field.getAnnotation(ConfigProp.class);
            ConfigProperty property = new ConfigProperty(object,new PropertyDescriptor(field.getName(),object.getClass()),propDefinition);
            list.add(property);
        }
        return list;
    }

    public static PropertyEditor<?> createFileImportableEditor(ConfigProperty property, AppComponent parent) {
        ServiceLoader<IconSPIService> iconSPIServices = ServiceLoader.load(IconSPIService.class);
        IconSPIService spiService = iconSPIServices.findFirst().orElse(null);
        Class iconSpiClazz = spiService.getClass();
        spiService = (IconSPIService) parent.findComponent(iconSpiClazz);

        ConfigProp propData = property.getPropData();
        ObservableList<String> files = FXCollections.observableArrayList();

        HBox hBox = new HBox();
        hBox.setSpacing(8);
        hBox.setAlignment(Pos.CENTER);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(files);
        comboBox.setPrefHeight(28);
        comboBox.getStyleClass().add("comb");
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        Button buttonImport = new Button();
        buttonImport.getStyleClass().add("btn");
        buttonImport.setFont(spiService.getFont());
        buttonImport.setText(spiService.getNamedIcon(IconSPIService.IconSet.OPEN));

        files.clear();
        File folder = new File(propData.value());
        for(File file : folder.listFiles()) {
            if (file.isFile()) {
                files.add(file.getName());
            }
        }

        if (propData.resolver() != PropResolver.class) {
            try {
                PropResolver importer = propData.resolver().newInstance();
                buttonImport.setOnAction(e -> {
                    FileChooser chooser = new FileChooser();
                    chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(importer.supportName(),importer.extensions()));
                    File file = chooser.showOpenDialog(null);
                    try {
                        importer.resolve(file);
                        files.clear();
                        for(File item : folder.listFiles()) {
                            if (item.isFile()) {
                                files.add(file.getName());
                            }
                        }
                        //UIUtil.showAlertDialog("资源导入成功。", "导入成功", Alert.AlertType.INFORMATION,config);
                    } catch (Exception ex) {
                        //UIUtil.showAlertDialog("资源导入失败。", "导入失败", Alert.AlertType.ERROR,config);
                    }

                });
            } catch (Exception ex) {
                logger.error("fail import resource :",ex);
            }
        } else {
            buttonImport.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("导入");
                File target = chooser.showOpenDialog(null);
                if (target == null || !target.exists()) {
                    return;
                }
                try {
                    Path pathTarget = Paths.get(target.getAbsolutePath());
                    Path copied = Paths.get(new File(folder.getPath() + "/" + target.getName()).getAbsolutePath());
                    Files.copy(pathTarget,copied);
                    files.clear();
                    for(File file : folder.listFiles()) {
                        if (file.isFile()) {
                            files.add(file.getName());
                        }
                    }
                } catch (IOException ex) {
                    logger.error("fail to import resource:", ex);
                    //UIUtil.showAlertDialog("导入资源失败", "提示", Alert.AlertType.ERROR, config);
                }
            });
        }
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().equals("")) {
                property.setValue(newValue);
            }
        });
        hBox.getChildren().addAll(comboBox, buttonImport);
        hBox.widthProperty().addListener(((observable, oldValue, newValue) -> {
            comboBox.setPrefWidth(hBox.getWidth() - buttonImport.getWidth());
        }));
        return new PropertyEditor() {

            @Override
            public Node getEditor() {
                return hBox;
            }

            @Override
            public Object getValue() {
                return comboBox.getSelectionModel().getSelectedItem();
            }

            @Override
            public void setValue(Object o) {
                comboBox.getSelectionModel().select(o.toString());
            }
        };
    }

    public static PropertyEditor<?> createFolderImportableEditor(ConfigProperty property, AppComponent parent) {
        ServiceLoader<IconSPIService> iconSPIServices = ServiceLoader.load(IconSPIService.class);
        IconSPIService spiService = iconSPIServices.findFirst().orElse(null);
        Class iconSpiClazz = spiService.getClass();
        spiService = (IconSPIService) parent.findComponent(iconSpiClazz);
        ConfigProp propData = property.getPropData();
        ObservableList<String> files = FXCollections.observableArrayList();

        HBox hBox = new HBox();
        hBox.setSpacing(8);
        hBox.setAlignment(Pos.CENTER);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(files);
        comboBox.setPrefHeight(28);
        comboBox.getStyleClass().add("comb");
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        Button buttonImport = new Button();
        buttonImport.getStyleClass().add("btn");
        buttonImport.setFont(spiService.getFont());
        buttonImport.setText(spiService.getNamedIcon(IconSPIService.IconSet.OPEN));

        files.clear();
        File folder = new File(propData.value());
        for(File file : folder.listFiles()) {
            if (file.isDirectory()) {
                files.add(file.getName());
            }
        }

        if (propData.resolver() != PropResolver.class) {
            try {
                PropResolver importer = propData.resolver().newInstance();
                buttonImport.setOnAction(e -> {
                    FileChooser chooser = new FileChooser();
                    chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(importer.supportName(),importer.extensions()));
                    File file = chooser.showOpenDialog(null);
                    try {
                        importer.resolve(file);
                        files.clear();
                        for(File fileItem : folder.listFiles()) {
                            if (fileItem.isDirectory()) {
                                files.add(file.getName());
                            }
                        }
                        // UIUtil.showAlertDialog("资源导入成功。", "导入成功", Alert.AlertType.INFORMATION,config);
                    } catch (Exception ex){
                       //  UIUtil.showAlertDialog("资源导入失败。", "导入失败", Alert.AlertType.ERROR,config);
                    }
                });
            } catch (Exception ex) {
                logger.error("fail to import resources",ex);
            }
        }

        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().equals("")) {
                property.setValue(newValue);
            }
        });

        hBox.getChildren().addAll(comboBox, buttonImport);
        hBox.widthProperty().addListener(((observable, oldValue, newValue) -> {
            comboBox.setPrefWidth(hBox.getWidth() - buttonImport.getWidth());
        }));
        return new PropertyEditor() {

            @Override
            public Node getEditor() {
                return hBox;
            }

            @Override
            public Object getValue() {
                return comboBox.getSelectionModel().getSelectedItem();
            }

            @Override
            public void setValue(Object o) {
                comboBox.getSelectionModel().select(o.toString());
            }
        };
    }

    public static PropertyEditor<?> createNumberRangeEditor(ConfigProperty property) {
        ConfigProp prop = property.getPropData();
        HBox hBox = new HBox();
        hBox.setSpacing(8);
        hBox.setAlignment(Pos.CENTER);

        Slider slider = new Slider();
        slider.setMax(Double.valueOf(prop.value()));
        slider.setValue(Double.valueOf(property.getValue().toString()));
        slider.setPrefHeight(28);
        slider.setMin(18);

        HBox.setHgrow(slider, Priority.ALWAYS);

        TextField text = new TextField();
        text.getStyleClass().add("txt");
        text.setText(property.getValue().toString());
        text.setPrefHeight(28);
        text.setPrefWidth(80);

        hBox.getChildren().addAll(slider, text);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (property.getType() == Integer.class || property.getType() == int.class) {
                text.setText(newValue.intValue() + "");
                property.setValue(newValue.intValue());
            } else if (property.getType() == Float.class || property.getType() == float.class){
                text.setText(newValue.floatValue() + "");
                property.setValue(newValue.floatValue());
            } else if (property.getType() == Double.class || property.getType() == double.class) {
                text.setText(newValue.doubleValue() +  "");
                property.setValue(newValue.doubleValue());
            }
        });

        text.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().equals("")) {
                    return;
                }
                slider.setValue(Double.valueOf(newValue));
                if (property.getType() == Integer.class || property.getType() == int.class) {
                    property.setValue(Integer.valueOf(newValue));
                } else if (property.getType() == Float.class || property.getType() == float.class){
                    property.setValue(Float.valueOf(newValue));
                } else if (property.getType() == Double.class || property.getType() == double.class) {
                    property.setValue(Double.valueOf(newValue));
                }
            } catch (Exception e) {
                logger.error("fail to resolve data: ",e);
            }
        });

        return new PropertyEditor() {
            @Override
            public Node getEditor() {
                return hBox;
            }

            @Override
            public Object getValue() {
                return slider.getValue();
            }

            @Override
            public void setValue(Object o) {
                slider.setValue(Double.valueOf(o.toString()));
                text.setText(o.toString());
            }
        };
    }

    public static PropertyEditor<?> createNumberEditor(ConfigProperty property) {
        TextField textField = new TextField();
        textField.getStyleClass().add("txt");
        textField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (textField.getText().trim().equals("")) {
                return;
            }
            try {
                if (property.getType() == Integer.class || property.getType() == int.class) {
                    property.setValue(Integer.valueOf(newValue));
                } else if (property.getType() == Float.class || property.getType() == float.class){
                    property.setValue(Float.valueOf(newValue));
                } else if (property.getType() == Double.class || property.getType() == double.class) {
                    property.setValue(Double.valueOf(newValue));
                }
            } catch (Exception ex){
                textField.setText("");
            }
        }));
        return new PropertyEditor() {
            @Override
            public Node getEditor() {
                return textField;
            }

            @Override
            public Object getValue() {
                return textField.getText();
            }

            @Override
            public void setValue(Object o) {
                textField.setText(o.toString());
            }
        };
    }

    public static PropertyEditor<?> getEditor(PropertySheet.Item prop, AppComponent parent) {
        if (!(prop instanceof ConfigProperty)) {
            return null;
        }
        ConfigProperty property = (ConfigProperty) prop;
        ConfigProp propData = property.getPropData();
        switch (propData.type()) {
            case FILE_SELECT_IMPORTABLE:
                return PropertyEditors.createFileImportableEditor(property, parent);
            case FOLDER_SELECT_IMPORTABLE:
                return PropertyEditors.createFolderImportableEditor(property,parent);
            case CHECK:
                return PropertyEditors.createCheckedEditor(property);
            case COLOR:
                return PropertyEditors.createColorEditor(property);
            case NUMBER_SELECTABLE:
                return PropertyEditors.createNumberRangeEditor(property);
            case NUMBER:
                return PropertyEditors.createNumberEditor(property);
            case CUSTOM:
                try {
                    return propData.editor()
                            .getConstructor(ConfigProperty.class, AppComponent.class)
                            .newInstance(property,parent)
                            .createEditor();
                } catch (Exception e) {
                    logger.error("fail to create custom editr.",e);
                }
        }
        return null;
    }

}
