module fx.framework.jpa {
    requires fx.framework.core;
    requires org.hibernate.orm.core;
    requires slf4j.api;
    requires java.persistence;

    requires java.sql;
    requires net.bytebuddy;
    requires com.fasterxml.classmate;
    requires java.xml.bind;

    exports org.swdc.fx.jpa;
    exports org.swdc.fx.jpa.anno;
}