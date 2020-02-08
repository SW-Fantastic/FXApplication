package org.swdc.fx.anno;

import org.swdc.fx.FXSplash;
import org.swdc.fx.FXView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SFXApplication {

    Class<? extends FXSplash> splash();

    Class<? extends FXView> mainView();

    String assetsPath() default "./assets";

}
