package org.swdc.fx.anno;

import javafx.stage.StageStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {

    String value() default "";

    String title() default "";

    boolean resizeable() default false;

    String[] style() default "";

    boolean stage() default true;

    StageStyle stageStyle() default StageStyle.DECORATED;

}
