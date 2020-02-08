package org.swdc.fx.anno;

import org.swdc.fx.properties.BasicPropertiesResolver;
import org.swdc.fx.properties.PropertiesResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {
    String value();
    String prefix();
    Class<? extends PropertiesResolver> resolve() default BasicPropertiesResolver.class;
}
