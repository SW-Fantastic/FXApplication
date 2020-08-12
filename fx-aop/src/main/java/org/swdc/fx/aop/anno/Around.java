package org.swdc.fx.aop.anno;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {

    String pattern() default "";

    Class annotationWith() default Annotation.class;
}
