package org.swdc.fx.anno;

import org.swdc.fx.event.AppEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {

    Class<? extends AppEvent> value() default AppEvent.class;

}
