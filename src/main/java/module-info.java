module fx.framework.core {
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.controls;
    requires slf4j.api;
    requires lesscss.engine;

    exports org.swdc.fx;
    exports org.swdc.fx.properties;
    exports org.swdc.fx.anno;
    exports org.swdc.fx.services;
    exports org.swdc.fx.extra;
    exports org.swdc.fx.event;
    exports org.swdc.fx.scanner;
}