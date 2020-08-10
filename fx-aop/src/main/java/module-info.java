module fx.framework.aop {
    requires fx.framework.core;
    requires cglib;
    requires net.bytebuddy;
    requires org.objectweb.asm;

    exports org.swdc.fx.aop;
    exports org.swdc.fx.aop.anno;
}