module swdcfx {
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires slf4j.api;

    exports org.swdc.fx;
    exports org.swdc.fx.properties;
    exports org.swdc.fx.anno;
    exports org.swdc.fx.services;
}