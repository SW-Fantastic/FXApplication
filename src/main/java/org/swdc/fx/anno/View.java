package org.swdc.fx.anno;

import javafx.stage.StageStyle;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {

    String value() default "";

    String title() default "";

    boolean resizeable() default false;

    String[] style() default "";

    boolean stage() default true;

    StageStyle stageStyle() default StageStyle.DECORATED;

    boolean background() default false;

    boolean dialog() default false;

}
