module fx.framework.aop {
    requires fx.framework.core;
    requires net.bytebuddy;
    requires slf4j.api;

    exports org.swdc.fx.aop;
    exports org.swdc.fx.aop.anno;

    provides org.swdc.fx.extra.ExtraLoader with
            org.swdc.fx.aop.AspectLoader;

    uses org.swdc.fx.extra.ExtraLoader;
}