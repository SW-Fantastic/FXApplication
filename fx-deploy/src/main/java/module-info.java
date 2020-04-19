module fx.framework.deploy {
    requires dd.plist;
    requires slf4j.api;
    requires java.xml;
    requires swdc.extern.ice;

    requires java.management;

    exports org.swdc.fx.deploy;
    exports org.swdc.fx.deploy.association;
    exports org.swdc.fx.deploy.system;

}