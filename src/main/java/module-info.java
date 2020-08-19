module fx.framework.core {
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.fxml;
    requires javafx.swing;
    requires slf4j.api;
    requires jcl.over.slf4j;
    requires lesscss.engine;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;
    requires javafx.base;

    opens org.swdc.fx.net.data to com.fasterxml.jackson.databind;

    exports org.swdc.fx;
    exports org.swdc.fx.properties;
    exports org.swdc.fx.anno;
    exports org.swdc.fx.services;
    exports org.swdc.fx.extra;
    exports org.swdc.fx.event;
    exports org.swdc.fx.scanner;
    exports org.swdc.fx.net.data;
    exports org.swdc.fx.container;

    opens defaultView;

    uses org.swdc.fx.extra.IconSPIService;
    uses org.swdc.fx.extra.ExtraLoader;
}