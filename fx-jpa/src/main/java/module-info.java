module fx.framework.jpa {
    requires fx.framework.core;
    requires org.hibernate.orm.core;
    requires slf4j.api;
    requires java.persistence;

    requires java.sql;
    requires net.bytebuddy;
    requires com.fasterxml.classmate;
    requires java.xml.bind;
    requires static fx.framework.aop;

    exports org.swdc.fx.jpa;
    exports org.swdc.fx.jpa.anno;
    exports org.swdc.fx.jpa.aspect to fx.framework.aop;

    opens org.swdc.fx.jpa.aspect to fx.framework.core;

    provides org.swdc.fx.extra.ExtraLoader with org.swdc.fx.jpa.JPALoader;
}